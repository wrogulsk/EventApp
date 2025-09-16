package pl.coderslab.Controllers_front;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.coderslab.events.Event;
import pl.coderslab.events.EventService;
import pl.coderslab.events.dto.EditEventRequest;
import pl.coderslab.events.dto.UpdateEventRequest;
import pl.coderslab.locations.Location;
import pl.coderslab.locations.LocationService;
import pl.coderslab.registrations.Registration;
import pl.coderslab.registrations.RegistrationRepository;
import pl.coderslab.registrations.RegistrationService;
import pl.coderslab.registrations.RegistrationStatus;
import pl.coderslab.tags.Tag;
import pl.coderslab.tags.TagService;
import pl.coderslab.users.User;
import pl.coderslab.users.UserService;
import pl.coderslab.users.dto.UserResponse;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/organizer")
//@PreAuthorize("hasRole('ORGANIZER')")
public class OrganizerController {

    @Autowired
    private EventService eventService;
    @Autowired
    private UserService userService;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private TagService tagService;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RegistrationRepository registrationRepository;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {

        long totalEvents = eventService.getTotalEventsCount();
        long totalRegistrations = registrationService.getTotalRegistrationsCount();

        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalRegistrations", totalRegistrations);

        return "organizer/dashboard";
    }

    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "organizer/events";
    }

    @PostMapping("/events")
    public String createEvent(@ModelAttribute Event event, Authentication auth) {
        User organizer = userService.findByEmail(auth.getName());
        event.setOrganizer(organizer.getLastName());
        eventService.save(event);
        return "redirect:/organizer/dashboard";
    }

    @GetMapping("/events/create")
    public String createEventForm(Model model) {
        model.addAttribute("event", new Event());
        return "organizer/create-event";
    }


    @GetMapping("/events/{id}")
    public String eventDetail(@PathVariable Long id, Model model, Authentication auth) {
        Optional<Event> eventOpt = eventService.getEventById(id);

        if (eventOpt.isEmpty()) {
            return "redirect:/organizer/events?error=Event not found";
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

        return "organizer/event-details";
    }

    @GetMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Long id, Model model) {
        return eventService.getEventById(id)
                .map(event -> {
                    EditEventRequest editRequest = new EditEventRequest(
                            event.getId(),
                            event.getTitle(),
                            event.getOrganizer(),
                            event.getStartAt(),
                            event.getEndAt(),
                            event.getCapacity(),
                            event.getLocation() != null ? event.getLocation().getId() : null,  // ✅ Get ID
                            event.getUser() != null ? event.getUser().getId() : null,          // ✅ Get ID
                            event.getTags().stream().map(Tag::getId).collect(Collectors.toSet()) // ✅ Get IDs
                    );

                    model.addAttribute("editEventRequest", editRequest);
                    model.addAttribute("locations", locationService.getAllLocations());
                    List<UserResponse> users = userService.getUsers();
                    model.addAttribute("users", users);
                    System.err.println("Added " + users.size() + " users to model");
                    model.addAttribute("tags", tagService.getAllTags());
                    model.addAttribute("eventId", id);
                    return "organizer/edit-event";
                })
                .orElse("redirect:/organizer/events");
    }

    @PostMapping("/events/{id}/edit")
    public String updateEvent(@PathVariable Long id,
                              @ModelAttribute @Valid EditEventRequest editEventRequest,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("locations", locationService.getAllLocations());
            model.addAttribute("users", userService.getUsers());
            model.addAttribute("tags", tagService.getAllTags());
            model.addAttribute("eventId", id);
            return "organizer/editevent";
        }

        try {
            Location location = null;
            if (editEventRequest.locationId() != null) {
                location = locationService.getLocationById(editEventRequest.locationId());
            }

            User user = null;
            if (editEventRequest.userId() != null) {
                user = userService.getUserById(editEventRequest.userId());
                if (user == null) {
                    throw new IllegalArgumentException("User not found with ID: " + editEventRequest.userId());
                }
            }

            Set<Tag> tags = new HashSet<>();
            if (editEventRequest.tagIds() != null && !editEventRequest.tagIds().isEmpty()) {
                tags = tagService.getTagsByIds(editEventRequest.tagIds());
            }

            UpdateEventRequest updateRequest = new UpdateEventRequest(
                    editEventRequest.title(),
                    editEventRequest.organizer(),
                    editEventRequest.startAt(),
                    editEventRequest.endAt(),
                    editEventRequest.capacity(),
                    location,
                    user,
                    tags
            );

            eventService.updateEvent(editEventRequest.id(), updateRequest);
            redirectAttributes.addFlashAttribute("successMessage", "Event updated successfully!");
            return "redirect:/organizer/events";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("locations", locationService.getAllLocations());
            model.addAttribute("users", userService.getUsers());
            model.addAttribute("tags", tagService.getAllTags());
            model.addAttribute("eventId", id);
            return "organizer/edit-event";
        }
    }

    @GetMapping("/new-location")
    public String newLocations(Model model) {

        return "locations/new-location";
    }

    @GetMapping("/registrations")
    public String registrations(Model model) {
        model.addAttribute("registrations", registrationService.getAllRegistrations());
        return "organizer/registrations";
    }

    @DeleteMapping("/registrations/{registrationId}")
    public String deleteRegistration(@PathVariable Long registrationId,
                                     RedirectAttributes redirectAttributes) {
        try {
            Registration registration = registrationRepository.findById(registrationId)
                    .orElseThrow(() -> new EntityNotFoundException("Registration not found"));

            registration.setStatus(RegistrationStatus.CANCELLED);
            registrationRepository.save(registration);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Registration cancelled successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to cancel registration!");
        }
        return "redirect:/organizer/registrations";
    }

}