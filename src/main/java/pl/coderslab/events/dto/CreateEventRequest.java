package pl.coderslab.events.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import pl.coderslab.locations.Location;
import pl.coderslab.tags.Tag;
import pl.coderslab.users.User;

import java.time.LocalDateTime;
import java.util.Set;


public record CreateEventRequest(
        @NotBlank(message = "Title is required")
        @Size(min = 5, max = 80, message = "Title must be between 5 - 80 characters")
        String title,

        @NotBlank(message = "Organizer is required")
        @Size(min = 5, max = 50, message = "Organizer must be between 5 - 80 characters")
        String organizer,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be in the future and can't be the same as the other event")
        LocalDateTime startAt,

        @NotNull(message = "Event date is required")
        @Future(message = "Event date must be in the future nd can't be the same as the other event")
        LocalDateTime endAt,

        @NotNull(message = "Amount of participants can't be empty")
        Integer capacity,

        @NotNull(message = "Choose location")
        Location location,

        @NotNull(message = "User is needed")
        User user,

        @NotNull(message = "At least one tag is required")
        Set<Tag>tags
) {
    public void setTitle(String title) {}

        public Object getEventDate() {
                return startAt;
        }

        public String getTitle() {
            return title;
        }

}
