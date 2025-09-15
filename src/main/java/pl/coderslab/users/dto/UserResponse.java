package pl.coderslab.users.dto;

import pl.coderslab.notifications.Notification;
import pl.coderslab.registrations.Registration;

import java.util.Set;

public record UserResponse(
        Long id,
        String firstName,
        String lastName,
        String email,
        Set<String> roles,
        Set<String> events
) {}