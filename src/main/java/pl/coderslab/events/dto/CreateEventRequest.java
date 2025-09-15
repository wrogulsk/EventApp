package pl.coderslab.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Setter;
import pl.coderslab.locations.Location;
import pl.coderslab.tags.Tag;
import pl.coderslab.users.User;

import java.time.LocalDateTime;
import java.util.Set;

public record CreateEventRequest(
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

        @NotBlank(message = " Choose location")
        Location location,

        @NotBlank(message = "User is needed")
        User user,

        @NotBlank(message = "At least one tag is required")
        Set<Tag>tags
) {
}
