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

    @GetMapping
    public ResponseEntity<List<RegistrationDTO>> getRegistrations() {
        List<RegistrationDTO> registrations = registrationService.getAllRegistrations().stream()
                .map(reg -> new RegistrationDTO(
                        reg.getId(),
                        reg.getStatus().name(),
                        reg.getRegisteredAt(),
                        reg.getUser().getId(),
                        reg.getUser().getFirstName(),
                        reg.getUser().getLastName(),
                        reg.getEvent().getId(),
                        reg.getEvent().getTitle()
                ))
                .toList();

        return ResponseEntity.ok(registrations);

    }

    @PostMapping("/register")
    public ResponseEntity<?> registerForEvent(
            @RequestParam Long userId,
            @RequestParam Long eventId) {
        try {
            registrationService.registerUserForEvent(userId, eventId);
            return ResponseEntity.ok("Registration successful");
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

    @DeleteMapping("/{id}/delete")
    public ResponseEntity<Void> deleteRegistration(@PathVariable Long id) {
        registrationService.deleteRegistration(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<RegistrationUserDto>> getUserRegistrations(@PathVariable Long userId) {
        List<RegistrationUserDto> registrations = registrationService.getUserRegistrations(userId).stream()
                .map(registration -> new RegistrationUserDto(
                        registration.getId(),
                        registration.getStatus().name(),
                        registration.getRegisteredAt(),
                        registration.getEvent().getId(),
                        registration.getEvent().getTitle()
                ))
                .toList();
        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/event/{eventId}/participants")
    public ResponseEntity<List<Registration>> getEventParticipants(@PathVariable Long eventId) {
        List<Registration> participants = registrationService.getEventParticipants(eventId);
        return ResponseEntity.ok(participants);
    }
}
