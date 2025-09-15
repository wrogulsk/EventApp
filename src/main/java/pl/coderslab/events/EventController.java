package pl.coderslab.events;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.events.dto.CreateEventRequest;
import pl.coderslab.events.dto.EventResponse;
import pl.coderslab.events.dto.UpdateEventRequest;
import pl.coderslab.locations.Location;
import pl.coderslab.registrations.Registration;
import pl.coderslab.users.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


@RestController
@RequestMapping("/events")
public class EventController {

    private EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<EventResponse>> getAllEvents() {
        List<EventResponse> list = eventService.getAllEvents();

        if (list.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("organizer/{organizer}")
    public ResponseEntity<Event> getEventsByOrganizer(@PathVariable String organizer) {
        return eventService.getEventsByOrganizer(organizer)
                .stream().map(ResponseEntity::ok).findFirst().orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/cities/{city}")
    public ResponseEntity<EventResponse> getEventsByCity(@PathVariable String city) {
        return eventService.getEventsByCity(city)
                .stream().map(ResponseEntity::ok).findFirst().orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("locations/{location}")
    public ResponseEntity<Event> getEventsByLocation(@PathVariable Location location) {
        return eventService.getEventByLocation(location)
                .stream().map(ResponseEntity::ok).findFirst().orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/add")
    public ResponseEntity<?> createEvent(@Valid @RequestBody CreateEventRequest createEventRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            bindingResult.getFieldErrors().forEach(error ->
                    errors.put(error.getField(), error.getDefaultMessage())
            );
            return ResponseEntity.badRequest().body(Map.of("validationErrors", errors));
        }

        try {
            Long id = eventService.createEvent(createEventRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "eventId", id,
                    "message", "Event created successfully"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "type", "BUSINESS_LOGIC_ERROR"
            ));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Database constraint violation: " + e.getMostSpecificCause().getMessage(),
                    "type", "DATABASE_ERROR"
            ));
        } catch (Exception e) {
            // TEMPORARILY show full error for debugging
            e.printStackTrace(); // This will print to console

            return ResponseEntity.internalServerError().body(Map.of(
                    "error", e.getMessage(),
                    "type", e.getClass().getSimpleName(),
                    "cause", e.getCause() != null ? e.getCause().getMessage() : "No cause"
            ));
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Long> updateEvent(@PathVariable Long id, @RequestBody UpdateEventRequest updateEventRequest) {
        Long newId = eventService.updateEvent(id, updateEventRequest);
        return ResponseEntity.status(HttpStatus.OK).body(newId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        if (eventService.getEventById(id).isPresent()) {
            eventService.deleteEvent(id);
            return ResponseEntity.status(HttpStatus.OK).body("Event deleted successfully");
        }
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
