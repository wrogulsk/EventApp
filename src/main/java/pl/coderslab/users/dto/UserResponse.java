package pl.coderslab.users.dto;

import pl.coderslab.events.Event;
import pl.coderslab.notifications.Notification;
import pl.coderslab.registrations.Registration;
import pl.coderslab.users.User;

import java.util.Set;
import java.util.stream.Collectors;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles
) {
    public static UserResponse fromUser(User user) {

        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRoleNames()
        );
    }
}