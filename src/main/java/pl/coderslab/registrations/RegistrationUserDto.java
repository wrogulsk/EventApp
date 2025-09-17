package pl.coderslab.registrations;

import java.time.LocalDateTime;

public record RegistrationUserDto(
        Long id,
        String lastName,
        String status,
        LocalDateTime registeredAt,
        Long eventId,
        String eventTitle
) {
}
