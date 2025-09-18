package pl.coderslab.notifications;

import pl.coderslab.events.Event;
import pl.coderslab.users.User;

public record NotificationDTO(
        String userLastName,
        String eventTitle,
        String message,
        Boolean isRead
) {
}
