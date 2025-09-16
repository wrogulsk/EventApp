package pl.coderslab.registrations;

import java.time.LocalDateTime;

public record RegistrationDTO(
        Long id,
        String status,
        LocalDateTime registeredAt,
        Long userId,
        String userFirstName,
        String userLastName,
        Long eventId,
        String eventTitle
) {
}
