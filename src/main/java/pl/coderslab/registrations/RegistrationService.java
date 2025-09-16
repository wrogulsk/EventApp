package pl.coderslab.registrations;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import pl.coderslab.events.Event;
import pl.coderslab.events.EventRepository;
import pl.coderslab.notifications.NotificationService;
import pl.coderslab.users.User;
import pl.coderslab.users.UserRepository;

import java.util.List;
import java.util.Optional;

@Service
public class RegistrationService {
    private final RegistrationRepository registrationRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public RegistrationService(RegistrationRepository registrationRepository,
                               EventRepository eventRepository,
                               UserRepository userRepository, NotificationService notificationService) {
        this.registrationRepository = registrationRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<Registration> getAllRegistrations() {
        return registrationRepository.findAll();
    }

    @Transactional
    public void registerUserForEvent(Long userId, Long eventId) {
        System.out.println(">>> [DEBUG] Starting registerUserForEvent for userId=" + userId + ", eventId=" + eventId);

        // Load participant
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        System.out.println(">>> [DEBUG] Found participant: " + user.getEmail());

        // Load event
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));
        System.out.println(">>> [DEBUG] Found event: " + event.getTitle());

        // Check if already registered
        if (registrationRepository.existsByUserAndEvent(user, event)) {
            throw new IllegalStateException("User already registered for this event");
        }

        // Check if event is full
        long currentRegistrations = registrationRepository.countByEventAndStatus(event, RegistrationStatus.CONFIRMED);
        if (currentRegistrations >= event.getCapacity()) {
            throw new IllegalStateException("Event is full");
        }

        // Create and save registration
        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setStatus(RegistrationStatus.CONFIRMED);
        registrationRepository.save(registration);
        System.out.println(">>> [DEBUG] Registration saved for userId=" + userId);

        // Always notify participant
        String participantMessage = String.format(
                "Zostałeś pomyślnie zarejestrowany na event: %s", event.getTitle()
        );
        notificationService.createNotification(userId, participantMessage, eventId);
        System.out.println(">>> [DEBUG] Notification sent to participant");

        // Notify organizer if different from participant
        Long organizerId = event.getUser().getId();
        if (!organizerId.equals(userId)) {
            String organizerMessage = String.format(
                    "Nowy uczestnik %s %s zapisał się na Twój event: %s",
                    user.getFirstName(),
                    user.getLastName(),
                    event.getTitle()
            );
            notificationService.createNotification(organizerId, organizerMessage, eventId);
            System.out.println(">>> [DEBUG] Notification sent to organizer");
        }

        System.out.println(">>> [DEBUG] registerUserForEvent completed successfully");
    }

    @Transactional
    public void cancelRegistration(Long registrationId, Long userId) {
        Registration registration = registrationRepository.findById(registrationId)
                .orElseThrow(() -> new EntityNotFoundException("Registration not found"));

        if (!registration.getUser().getId().equals(userId)) {
            throw new SecurityException("Cannot cancel other user's registration");
        }

        registration.setStatus(RegistrationStatus.CANCELLED);
        notificationService.notifyRegistrationCancelled(userId, registration.getEvent().getId());
        registrationRepository.save(registration);
    }

    public List<Registration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserIdAndStatus(userId, RegistrationStatus.CONFIRMED);
    }

    public List<Registration> getEventParticipants(Long eventId) {
        return registrationRepository.findByEventIdAndStatus(eventId, RegistrationStatus.CONFIRMED);
    }

    public boolean isUserRegistered(Long userId, Long eventId) {
        return registrationRepository.existsByUserIdAndEventIdAndStatus(
                userId, eventId, RegistrationStatus.CONFIRMED);
    }

    public int getAvailableSpots(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        long confirmedRegistrations = registrationRepository.countByEventAndStatus(event, RegistrationStatus.CONFIRMED);
        return Math.max(0, event.getCapacity() - (int) confirmedRegistrations);
    }

    public void unregisterUserFromEvent(Long userId, Long eventId) {
        Optional<Registration> registration = registrationRepository
                .findByEventIdAndUserId(eventId, userId);

        if (registration.isPresent()) {
            registrationRepository.delete(registration.get());
        } else {
            throw new RuntimeException("Registration not found");
        }
    }

    public long getTotalRegistrationsCount() {
        return registrationRepository.count();
    }
}
