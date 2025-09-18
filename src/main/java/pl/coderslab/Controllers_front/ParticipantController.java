package pl.coderslab.Controllers_front;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.coderslab.events.Event;
import pl.coderslab.events.EventService;
import pl.coderslab.locations.Location;
import pl.coderslab.locations.LocationService;
import pl.coderslab.registrations.Registration;
import pl.coderslab.registrations.RegistrationService;
import pl.coderslab.tags.Tag;
import pl.coderslab.tags.TagService;
import pl.coderslab.users.User;
import pl.coderslab.users.UserService;

import java.util.*;

@Controller
@RequestMapping("/participant")

public class ParticipantController {

    private final EventService eventService;
    private final RegistrationService registrationService;
    private final UserService userService;
    private final LocationService locationService;
    private final TagService tagService;
    private final PasswordEncoder passwordEncoder;

    public ParticipantController(EventService eventService, RegistrationService registrationService, UserService userService, LocationService locationService, TagService tagService, PasswordEncoder passwordEncoder) {
        this.eventService = eventService;
        this.registrationService = registrationService;
        this.userService = userService;
        this.locationService = locationService;
        this.tagService = tagService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/events")
    public String events(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) Long tagId,
            @RequestParam(defaultValue = "startAt") String sortBy,
            Model model,
            Authentication auth) {

        try {
            List<Event> events = new ArrayList<>();
            List<Location> locations = new ArrayList<>();
            List<Tag> tags = new ArrayList<>();
            User currentUser = null;

            try {
                events = eventService.findEventsWithFilters(search, locationId, tagId, sortBy);
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Error loading events. Showing empty list.");
            }

            try {
                locations = locationService.getAllLocations();
            } catch (Exception e) {
                System.err.println("Error loading locations: " + e.getMessage());
            }

            try {
                tags = tagService.findAll();
            } catch (Exception e) {
                System.err.println("Error loading tags: " + e.getMessage());
            }

            if (auth != null && auth.isAuthenticated()) {
                try {
                    currentUser = userService.findByEmail(auth.getName());
                } catch (Exception e) {
                    System.err.println("Error loading current user: " + e.getMessage());
                }
            }

            model.addAttribute("events", events);
            model.addAttribute("locations", locations);
            model.addAttribute("tags", tags);
            model.addAttribute("currentUser", currentUser);

            return "participant/events";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Critical error loading page: " + e.getMessage());
            model.addAttribute("events", new ArrayList<>());
            model.addAttribute("locations", new ArrayList<>());
            model.addAttribute("tags", new ArrayList<>());
            model.addAttribute("currentUser", null);

            return "participant/events";
        }
    }

    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, Model model, Authentication auth) {
        Optional<Event> eventOpt = eventService.getEventById(id);

        if (eventOpt.isEmpty()) {
            return "redirect:/participant/events?error=Event not found";
        }

        Event event = eventOpt.get();
        User participant = userService.findByEmail(auth.getName());

        List<Registration> registrations = eventService.getEventRegistrations(id);
        boolean isUserRegistered = eventService.isUserRegisteredForEvent(participant.getId(), id);

        model.addAttribute("e", event);
        model.addAttribute("registrationCount", registrations.size());
        model.addAttribute("registrations", registrations);
        model.addAttribute("isRegistered", isUserRegistered);
        model.addAttribute("user", participant);

        return "participant/event-details";
    }


    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {

        try {
            User participant = userService.findByEmail(auth.getName());
            Optional<Event> eventOpt = eventService.getEventById(id);

            Event event = eventOpt.get();

            if (event.isUserRegistered(participant.getId())) {
                redirectAttributes.addFlashAttribute("warningMessage",
                        "Jesteś już zarejestrowany na to wydarzenie!");
                return "redirect:/participant/events";
            }

            if (event.isFull()) {
                redirectAttributes.addFlashAttribute("errorMessage",
                        "Wydarzenie jest już pełne!");
                return "redirect:/participant/events";
            }

            registrationService.registerUserForEvent(participant.getId(), event.getId());

            redirectAttributes.addFlashAttribute("successMessage",
                    "Zarejestrowałeś się na wydarzenie: " + event.getTitle());

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Błąd podczas rejestracji: " + e.getMessage());
        }

        return "redirect:/participant/events";
    }


    @PostMapping("/events/{id}/unregister")
    public String unregisterFromEvent(@PathVariable Long id,
                                      Authentication auth,
                                      RedirectAttributes redirectAttributes) {
        try {
            User participant = userService.findByEmail(auth.getName());

            registrationService.unregisterUserFromEvent(participant.getId(), id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Successfully unregistered from the event!");

        } catch (Exception e) {
            System.out.println("Unregister error: " + e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Unregistration failed: " + e.getMessage());
        }

        return "redirect:/participant/events/" + id;
    }

    @GetMapping("/profile")
    public String profile(Model model, Authentication auth) {
        try {
            User user = userService.findByEmail(auth.getName());
            model.addAttribute("user", user);

            return "participant/profile";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading profile: " + e.getMessage());
            return "redirect:/dashboard";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam String email,
                                @RequestParam(required = false) String phoneNumber,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(auth.getName());

            if (firstName == null || firstName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "First name is required");
                return "redirect:/participant/profile";
            }

            if (lastName == null || lastName.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Last name is required");
                return "redirect:/participant/profile";
            }

            if (email == null || email.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email is required");
                return "redirect:/participant/profile";
            }

            if (!user.getEmail().equals(email.trim())) {
                User existingUser = userService.findByEmail(email.trim());
                if (existingUser != null && !existingUser.getId().equals(user.getId())) {
                    redirectAttributes.addFlashAttribute("errorMessage", "Email is already in use");
                    return "redirect:/participant/profile";
                }
            }

            user.setFirstName(firstName.trim());
            user.setLastName(lastName.trim());
            user.setEmail(email.trim());

            userService.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
            return "redirect:/participant/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating profile: " + e.getMessage());
            return "redirect:/participant/profile";
        }
    }

    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth,
                                 RedirectAttributes redirectAttributes) {

        try {
            User user = userService.findByEmail(auth.getName());

            // Walidacja
            if (currentPassword == null || currentPassword.trim().isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is required");
                return "redirect:/participant/profile";
            }

            if (newPassword == null || newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters long");
                return "redirect:/participant/profile";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match");
                return "redirect:/participant/profile";
            }

            if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect");
                return "redirect:/participant/profile";
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            userService.save(user);

            redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
            return "redirect:/participant/profile";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error changing password: " + e.getMessage());
            return "redirect:/participant/profile";
        }
    }

    @GetMapping("/registrations")
    public String myRegistrations(Model model, Authentication auth) {
        try {
            User participant = userService.findByEmail(auth.getName());

            List<Registration> registrations = registrationService.getUserRegistrations(participant.getId());

            model.addAttribute("registrations", registrations);
            model.addAttribute("currentUser", participant); // Użyj tego samego obiektu
            model.addAttribute("user", participant); // Dodaj też jako 'user' dla spójności

            return "participant/registrations";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error loading registrations: " + e.getMessage());
            model.addAttribute("registrations", new ArrayList<>()); // Pusta lista
            return "participant/registrations";
        }

    }

}
