package pl.coderslab.invitations;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.coderslab.events.Event;
import pl.coderslab.events.EventRepository;
import pl.coderslab.notifications.NotificationService;
import pl.coderslab.registrations.RegistrationService;
import pl.coderslab.users.User;
import pl.coderslab.users.UserRepository;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class InvitationService {

    private final InvitationRepository invitationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final RegistrationService registrationService;

    public InvitationService(InvitationRepository invitationRepository,
                             EventRepository eventRepository,
                             UserRepository userRepository,
                             NotificationService notificationService,
                             RegistrationService registrationService) {
        this.invitationRepository = invitationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
        this.registrationService = registrationService;
    }

    // Wysyłanie zaproszeń
    public Invitation sendInvitation(Long eventId, String email, Long invitedByUserId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Sprawdź czy zaproszenie już istnieje
        Optional<Invitation> existingInvitation = invitationRepository
                .findByEventIdAndEmailAndStatusNot(eventId, email, InvitationStatus.DECLINED);

        if (existingInvitation.isPresent()) {
            throw new IllegalStateException("Invitation already sent to this email for this event");
        }

        Invitation invitation = new Invitation();
        invitation.setEmail(email);
        invitation.setEvent(event);
        invitation.setStatus(InvitationStatus.PENDING);
        invitation.setSentAt(LocalDateTime.now());

        return invitationRepository.save(invitation);
    }

    // Wysyłanie wielu zaproszeń
    public List<Invitation> sendMultipleInvitations(Long eventId, List<String> emails, Long invitedByUserId) {
        List<Invitation> invitations = new ArrayList<>();

        for (String email : emails) {
            try {
                Invitation invitation = sendInvitation(eventId, email, invitedByUserId);
                invitations.add(invitation);
            } catch (IllegalStateException e) {
                // Loguj błąd ale kontynuuj z pozostałymi emailami
                System.err.println("Failed to send invitation to " + email + ": " + e.getMessage());
            }
        }

        return invitations;
    }

    // Odpowiedź na zaproszenie
    public Invitation respondToInvitation(Long invitationId, InvitationStatus response) {
        if (response != InvitationStatus.ACCEPTED && response != InvitationStatus.DECLINED) {
            throw new IllegalArgumentException("Response must be ACCEPTED or DECLINED");
        }

        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Invitation has already been responded to");
        }

        invitation.setStatus(response);
        invitation.setRespondedAt(LocalDateTime.now());

        // Jeśli zaakceptowano i użytkownik istnieje, automatycznie zarejestruj
        if (response == InvitationStatus.ACCEPTED && invitation.getUser() != null) {
            try {
                registrationService.registerUserForEvent(
                        invitation.getUser().getId(),
                        invitation.getEvent().getId()
                );
            } catch (Exception e) {
                // Jeśli rejestracja się nie powiodła (np. brak miejsc), zachowaj akceptację zaproszenia
                System.err.println("Failed to auto-register user: " + e.getMessage());
            }
        }

        return invitationRepository.save(invitation);
    }

    // Pobieranie zaproszeń
    public List<Invitation> getInvitationsForEvent(Long eventId) {
        return invitationRepository.findByEventIdOrderBySentAtDesc(eventId);
    }

    public List<Invitation> getInvitationsForUser(Long userId) {
        return invitationRepository.findByUserIdOrderBySentAtDesc(userId);
    }

    public List<Invitation> getInvitationsByEmail(String email) {
        return invitationRepository.findByEmailOrderBySentAtDesc(email);
    }

    public List<Invitation> getPendingInvitations(Long eventId) {
        return invitationRepository.findByEventIdAndStatusOrderBySentAtDesc(eventId, InvitationStatus.PENDING);
    }

    public List<Invitation> getPendingInvitationsForUser(Long userId) {
        return invitationRepository.findByUserIdAndStatusOrderBySentAtDesc(userId, InvitationStatus.PENDING);
    }

    // Anulowanie zaproszenia
    public void cancelInvitation(Long invitationId, Long cancelledByUserId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Can only cancel pending invitations");
        }

        invitationRepository.delete(invitation);

        // Powiadom użytkownika o anulowaniu (jeśli istnieje)
        if (invitation.getUser() != null) {
            String message = String.format("Zaproszenie na event '%s' zostało anulowane",
                    invitation.getEvent().getTitle());
            notificationService.createNotification(invitation.getUser().getId(), message, invitation.getEvent().getId());
        }
    }

    // Ponowne wysłanie zaproszenia
    public Invitation resendInvitation(Long invitationId) {
        Invitation invitation = invitationRepository.findById(invitationId)
                .orElseThrow(() -> new EntityNotFoundException("Invitation not found"));

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new IllegalStateException("Can only resend pending invitations");
        }

        invitation.setSentAt(LocalDateTime.now());

        // Wyślij powiadomienie ponownie (jeśli użytkownik istnieje)
        if (invitation.getUser() != null) {
            String message = String.format("Przypomnienie: Zaproszenie na event '%s'",
                    invitation.getEvent().getTitle());
            notificationService.createNotification(invitation.getUser().getId(), message, invitation.getEvent().getId());
        }

        return invitationRepository.save(invitation);
    }

    // Statystyki
    public long getInvitationCount(Long eventId, InvitationStatus status) {
        if (status != null) {
            return invitationRepository.countByEventIdAndStatus(eventId, status);
        }
        return invitationRepository.countByEventId(eventId);
    }

    public Map<InvitationStatus, Long> getInvitationStatistics(Long eventId) {
        Map<InvitationStatus, Long> stats = new HashMap<>();
        for (InvitationStatus status : InvitationStatus.values()) {
            stats.put(status, invitationRepository.countByEventIdAndStatus(eventId, status));
        }
        return stats;
    }

    // Automatyczne wygaszanie starych zaproszeń
    @Scheduled(cron = "0 0 1 * * ?") // Codziennie o 1:00
    public void expireOldInvitations() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30); // 30 dni
        List<Invitation> expiredInvitations = invitationRepository
                .findBySentAtBeforeAndStatus(cutoffDate, InvitationStatus.PENDING);

        for (Invitation invitation : expiredInvitations) {
            invitation.setStatus(InvitationStatus.EXPIRED);
        }

        if (!expiredInvitations.isEmpty()) {
            invitationRepository.saveAll(expiredInvitations);
        }
    }
}
