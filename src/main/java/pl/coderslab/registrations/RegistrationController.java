package pl.coderslab.registrations;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.coderslab.events.EventService;
import pl.coderslab.users.UserService;

import java.util.List;

@RestController
@RequestMapping("/registrations")
public class RegistrationController {
    private final RegistrationService registrationService;
    private final EventService eventService;

    public RegistrationController(RegistrationService registrationService,  EventService eventService) {
        this.registrationService = registrationService;
        this.eventService = eventService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerForEvent(
            @RequestParam Long userId,
            @RequestParam Long eventId) {
        try {
            Registration registration = eventService.registerUserForEvent(userId, eventId);
            return ResponseEntity.ok(registration);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelRegistration(
            @PathVariable Long id,
            @RequestParam Long userId) {
        registrationService.cancelRegistration(id, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Registration>> getUserRegistrations(@PathVariable Long userId) {
        List<Registration> registrations = registrationService.getUserRegistrations(userId);
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/event/{eventId}/participants")
    public ResponseEntity<List<Registration>> getEventParticipants(@PathVariable Long eventId) {
        List<Registration> participants = registrationService.getEventParticipants(eventId);
        return ResponseEntity.ok(participants);
    }
}
