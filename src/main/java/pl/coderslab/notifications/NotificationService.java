package pl.coderslab.notifications;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.coderslab.events.Event;
import pl.coderslab.events.EventRepository;
import pl.coderslab.users.User;
import pl.coderslab.users.UserRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    public NotificationService(NotificationRepository notificationRepository, UserRepository userRepository, EventRepository eventRepository) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.eventRepository = eventRepository;
    }

    public Notification createNotification(Long userId, String message, Long eventId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);

        if (eventId != null) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new EntityNotFoundException("Event not found"));
            notification.setEvent(event);
        }

        return notificationRepository.save(notification);
    }

    // Powiadomienie bez eventu
    public Notification createNotification(Long userId, String message) {
        return createNotification(userId, message, null);
    }

    // Powiadomienia związane z rejestracją
    public void notifyRegistrationConfirmed(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        String message = String.format("Zostałeś pomyślnie zarejestrowany na event: %s", event.getTitle());
        createNotification(userId, message, eventId);
    }

    public void notifyRegistrationCancelled(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        String message = String.format("Twoja rejestracja na event '%s' została anulowana", event.getTitle());
        createNotification(userId, message, eventId);
    }

    // Powiadomienia o zmianach w evencie
    public void notifyEventUpdated(Long eventId, String changeDescription) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        // Znajdź wszystkich zarejestrowanych użytkowników
        List<User> registeredUsers = notificationRepository.findRegisteredUsersForEvent(eventId);

        String message = String.format("Event '%s' został zaktualizowany: %s",
                event.getTitle(), changeDescription);

        for (User user : registeredUsers) {
            createNotification(user.getId(), message, eventId);
        }
    }

    public void notifyEventCancelled(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<User> registeredUsers = notificationRepository.findRegisteredUsersForEvent(eventId);
        String message = String.format("Event '%s' został odwołany", event.getTitle());

        for (User user : registeredUsers) {
            createNotification(user.getId(), message, eventId);
        }
    }

    // Przypomnienia
    public void sendEventReminder(Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        List<User> registeredUsers = notificationRepository.findRegisteredUsersForEvent(eventId);
        String message = String.format("Przypomnienie: Event '%s' rozpocznie się %s",
                event.getTitle(),
                event.getStartAt().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")));

        for (User user : registeredUsers) {
            createNotification(user.getId(), message, eventId);
        }
    }

    // Pobieranie powiadomień użytkownika
    public List<Notification> getUserNotifications(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getUnreadNotifications(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    // Oznaczanie jako przeczytane
    public void markAsRead(Long notificationId, Long userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new EntityNotFoundException("Notification not found"));

        // Sprawdź czy powiadomienie należy do użytkownika
        if (!notification.getUser().getId().equals(userId)) {
            throw new SecurityException("Cannot access other user's notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    public void markAllAsRead(Long userId) {
        List<Notification> unreadNotifications = getUnreadNotifications(userId);
        for (Notification notification : unreadNotifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }

    // Liczba nieprzeczytanych
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    // Usuwanie starych powiadomień
    @Scheduled(cron = "0 0 2 * * ?") // Codziennie o 2:00
    public void cleanupOldNotifications() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);
        notificationRepository.deleteByCreatedAtBeforeAndIsReadTrue(cutoffDate);
    }


}
