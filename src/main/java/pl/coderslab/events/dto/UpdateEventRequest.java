package pl.coderslab.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import pl.coderslab.locations.Location;
import pl.coderslab.tags.Tag;
import pl.coderslab.users.User;

import java.time.LocalDateTime;
import java.util.Set;

public record UpdateEventRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 80, message = "Title too long")
        String title,

        @NotBlank(message = "Organizer is required")
        @Size(max = 50, message = "Too long")
        String organizer,

        @NotBlank(message = "The date can't be past")
        LocalDateTime startAt,

        @NotBlank(message = "The date can't be future")
        LocalDateTime endAt,

        @NotBlank(message = "Amount of participants can't be bigger than capacity of location")
        Integer capacity,

        @NotNull(message = "Location ID is required")
        Location location,

        @NotNull(message = "User ID is required")
        User user,

        Set<Tag> tags
) {
}
