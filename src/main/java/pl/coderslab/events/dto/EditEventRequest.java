package pl.coderslab.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.coderslab.locations.Location;
import pl.coderslab.tags.Tag;
import pl.coderslab.users.User;

import java.time.LocalDateTime;
import java.util.Set;

public record EditEventRequest(
        @NotNull(message = "Event ID is required")
        Long id,

        @NotBlank(message = "Event title is required")
        String title,

        String organizer,

        @NotNull(message = "Start date is required")
        LocalDateTime startAt,

        @NotNull(message = "End date is required")
        LocalDateTime endAt,

        Integer capacity,

        @NotNull(message = "Location ID is required")
        Long locationId,

        @NotNull(message = "User ID is required")
        Long userId,

        Set<Long> tagIds
) {}
