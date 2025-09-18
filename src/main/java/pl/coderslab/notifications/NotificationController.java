package pl.coderslab.notifications;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.registrations.RegistrationService;

import java.util.List;

@RestController
@RequestMapping("/notifications")
public class NotificationController {
    private final NotificationService notificationService;
    private final RegistrationService registrationService;

    public NotificationController(NotificationService notificationService, RegistrationService registrationService) {
        this.notificationService = notificationService;
        this.registrationService = registrationService;
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTO>> getUserNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUserNotifications(userId).stream()
                .map(notification -> new NotificationDTO(
                        notification.getUser().getLastName(),
                        notification.getEvent().getTitle() ,
                        notification.getMessage(),
                        notification.getIsRead()
                ))
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDTO>> getUnreadNotifications(@PathVariable Long userId) {
        List<NotificationDTO> notifications = notificationService.getUnreadNotifications(userId).stream()
                .map(notification -> new NotificationDTO(
                        notification.getUser().getLastName(),
                        notification.getEvent().getTitle() ,
                        notification.getMessage(),
                        notification.getIsRead()
                ))
                .toList();
        return ResponseEntity.ok(notifications);
    }

    @GetMapping("/user/{userId}/unread/count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable Long userId) {
        long count = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(count);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<String> markAsRead(@PathVariable Long id, @RequestParam Long userId) {
        notificationService.markAsRead(id, userId);
        return ResponseEntity.ok("Marked as read");
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<String> markAllAsRead(@PathVariable Long userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok("All notifications marked as read");
    }
}
