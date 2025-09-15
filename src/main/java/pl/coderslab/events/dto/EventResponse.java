package pl.coderslab.events.dto;

import pl.coderslab.locations.Location;
import pl.coderslab.tags.Tag;

import java.time.LocalDateTime;
import java.util.Set;

public record EventResponse(
        Long id,
        String title,
        String organizer,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Integer capacity,
        Location location,
        Set<Tag> tags
) {}
