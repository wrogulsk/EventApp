package pl.coderslab.events;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.events.dto.CreateEventRequest;
import pl.coderslab.events.dto.EventResponse;
import pl.coderslab.events.dto.UpdateEventRequest;
import pl.coderslab.locations.Location;
import pl.coderslab.registrations.Registration;
import pl.coderslab.users.User;

import java.util.List;
import java.util.Optional;


@RestController
@RequestMapping("/event")
public class EventController {

    private EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping("events/all")
    public List<EventResponse> getAllEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public Optional<Event> getEventById(@PathVariable Long id) {
        return eventService.getEventById(id);
    }

    @GetMapping("organizer/{organizer}")
    public List<Event> getEventsByOrganizer(@PathVariable String organizer) {
        return eventService.getEventsByOrganizer(organizer);
    }

    @GetMapping("locations/{location}")
    public List<Event> getEventsByLocation(@PathVariable Location location) {
        return eventService.getEventByLocation(location);
    }

    @PostMapping("/add")
    public ResponseEntity<Long> createEvent(@RequestBody CreateEventRequest createEventRequest) {
        Long id = eventService.createEvent(createEventRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<Long> updateEvent(@PathVariable Long id, @RequestBody UpdateEventRequest updateEventRequest) {
        Long newId = eventService.updateEvent(id, updateEventRequest);
        return ResponseEntity.status(HttpStatus.OK).body(newId);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id) {
        eventService.deleteEvent(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/cities/{city}")
    public List<EventResponse> getEventsByCity(@PathVariable String city) {
        return eventService.getEventsByCity(city);
    }

    @GetMapping("/registrations/{id}")
    public List<Registration> getEventsByRegistration(@PathVariable Long id) {
        return eventService.getEventRegistrations(id);
    }

    @GetMapping("/registrations/user/{id}")
    public List<Event> getEventRegistrationsByUserId(@PathVariable Long id) {
        return eventService.getEventsRegistrationsByUserId(id);
    }

}
