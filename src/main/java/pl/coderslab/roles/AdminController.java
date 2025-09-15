package pl.coderslab.roles;

import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.coderslab.events.Event;
import pl.coderslab.events.EventService;
import pl.coderslab.events.dto.EditEventRequest;
import pl.coderslab.events.dto.EventResponse;
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
import pl.coderslab.users.dto.CreateUserRequest;
import pl.coderslab.users.dto.EditUserRequest;
import pl.coderslab.users.dto.UserResponse;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
//@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final EventService eventService;
    private final RoleService roleService;
    private final LocationService locationService;
    private final TagService tagService;
    private final RegistrationService registrationService;
    private final RegistrationRepository registrationRepository;

    public AdminController(UserService userService, EventService eventService, RoleService roleService, LocationService locationService, TagService tagService, RegistrationService registrationService, RegistrationRepository registrationRepository) {
        this.userService = userService;
        this.eventService = eventService;
        this.roleService = roleService;
        this.locationService = locationService;
        this.tagService = tagService;
        this.registrationService = registrationService;
        this.registrationRepository = registrationRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        long totalUsers = userService.getTotalUsersCount();
        long totalEvents = eventService.getTotalEventsCount();
        long totalLocations = locationService.getTotalLocationsCount();
        long totalRegistrations = registrationService.getTotalRegistrationsCount();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalEvents", totalEvents);
        model.addAttribute("totalLocations", totalLocations);
        model.addAttribute("totalRegistrations", totalRegistrations);
        return "admin/dashboard";
    }

    @GetMapping("/users")
    public String users(Model model) {
        model.addAttribute("users", userService.getUsers());
        return "admin/users";
    }

    @PostMapping("/users")
    public String createUser(@ModelAttribute @Valid CreateUserRequest createUserRequest,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            return "admin/newuser";
        }

        try {
            Long userId = userService.createUser(createUserRequest);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleService.getAllRoles());
            return "admin/newuser";
        }
    }

    @GetMapping("/newuser")
    public String newUser(Model model) {
        model.addAttribute("createUserRequest", new CreateUserRequest("", "", "", "", new HashSet<>()));
        model.addAttribute("roles", roleService.getAllRoles()); // For dropdown
        return "admin/newuser";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user. Please try again.");
        }
        return "redirect:/admin/users";
    }

    @GetMapping("/users/{id}/edit")
    public String editUser(@PathVariable Long id, Model model) {
        try {
            User user = userService.getUserById(id);
            EditUserRequest editRequest = EditUserRequest.fromUser(user);

            model.addAttribute("editUserRequest", editRequest);
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("userId", id);
            return "admin/edituser";
        } catch (Exception e) {
            return "redirect:/admin/users";
        }
    }

    @PostMapping("/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @ModelAttribute @Valid EditUserRequest editUserRequest,
                             BindingResult bindingResult,
                             Model model,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("userId", id);
            return "admin/edituser";
        }

        try {
            userService.updateUser(editUserRequest);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("roles", roleService.getAllRoles());
            model.addAttribute("userId", id);
            return "admin/edituser";
        }
    }

    @GetMapping("/events")
    public String events(Model model) {
        model.addAttribute("events", eventService.getAllEvents());
        return "admin/events";
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
                            event.getLocation() != null ? event.getLocation().getId() : null,
                            event.getUser() != null ? event.getUser().getId() : null,
                            event.getTags().stream().map(Tag::getId).collect(Collectors.toSet())
                    );

                    model.addAttribute("editEventRequest", editRequest);
                    model.addAttribute("locations", locationService.getAllLocations());
                    List<UserResponse> users = userService.getUsers();
                    model.addAttribute("users", users);
                    System.err.println("Added " + users.size() + " users to model");
                    model.addAttribute("tags", tagService.getAllTags());
                    model.addAttribute("eventId", id);
                    return "admin/editevent";
                })
                .orElse("redirect:/admin/events");
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
            return "admin/editevent";
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
            return "redirect:/admin/events";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("locations", locationService.getAllLocations());
            model.addAttribute("users", userService.getUsers());
            model.addAttribute("tags", tagService.getAllTags());
            model.addAttribute("eventId", id);
            return "admin/editevent";
        }
    }

    @GetMapping("/locations")
    public String locations(Model model) {
        model.addAttribute("locations", locationService.getAllLocations());
        return "admin/locations/list";
    }

    @GetMapping("/locations/{id}/edit")
    public String editLocation(@PathVariable Long id, Model model) {
        Location location = locationService.getLocationById(id);
        if (location == null) {
            return "redirect:/admin/locations";
        }

        model.addAttribute("location", location);
        return "admin/locations/edit";
    }

    @PostMapping("/locations/{id}/edit")
    public String updateLocation(@PathVariable Long id,
                                 @ModelAttribute @Valid Location location,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        location.setId(id);

        if (bindingResult.hasErrors()) {
            System.err.println("Validation errors found:");
            bindingResult.getAllErrors().forEach(error ->
                    System.err.println("- " + error.getDefaultMessage())
            );
            return "admin/locations/edit";
        }

        try {
            Location savedLocation = locationService.updateLocation(location.getId(), location);
            redirectAttributes.addFlashAttribute("successMessage", "Location updated successfully!");
            return "redirect:/admin/locations";

        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "admin/locations/edit";
        }
    }

    @GetMapping("/locations/create")
    public String createLocationForm(Model model) {
        Location location = new Location();
        location.setIsActive(true); // Default value
        model.addAttribute("location", location);
        return "admin/locations/create";
    }

    @PostMapping("/locations/create")
    public String createLocation(@ModelAttribute @Valid Location location,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "admin/locations/create";
        }

        try {
            locationService.createLocation(location);
            redirectAttributes.addFlashAttribute("successMessage", "Location created successfully!");
            return "redirect:/admin/locations";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Error: " + e.getMessage());
            return "admin/locations/create";
        }
    }

    @GetMapping("/locations/{id}")
    public String locationDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Location location = locationService.getLocationById(id);
            if (location == null) {
                redirectAttributes.addFlashAttribute("errorMessage", "Location not found!");
                return "redirect:/admin/locations";
            }

            model.addAttribute("location", location);

            if (location.getEvents() != null) {
                model.addAttribute("eventsCount", location.getEvents().size());
            }
            return "admin/locations/details";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error loading location: " + e.getMessage());
            return "redirect:/admin/locations";
        }
    }


    @GetMapping
    public String showEvents(@RequestParam(required = false) String city, Model model) {
        List<EventResponse> events;

        if (city != null && !city.isEmpty()) {
            events = eventService.getEventsByCity(city);
            model.addAttribute("selectedCity", city);
        } else {
            events = eventService.getAllEvents();
        }

        model.addAttribute("events", events);
        model.addAttribute("cities", eventService.getAllCities());

        return "admin/events";
    }

    @GetMapping("/registrations")
    public String registrations(Model model) {
        model.addAttribute("registrations", registrationService.getAllRegistrations());
        return "admin/registrations";
    }

    @GetMapping("/events/{eventId}/registrations")
    public String eventRegistrations(@PathVariable Long eventId, Model model) {
        Optional<Event> eventOptional = eventService.getEventById(eventId);

        if (eventOptional.isEmpty()) {
            model.addAttribute("error", "Event not found");
            return "admin/error";
        }

        Event event = eventOptional.get();
        List<Registration> registrations = registrationService.getEventParticipants(eventId);

        model.addAttribute("event", event);
        model.addAttribute("registrations", registrations);
        return "admin/event-registrations";
    }

    @DeleteMapping("/registrations/{registrationId}")
    public String deleteRegistration(@PathVariable Long registrationId,
                                     RedirectAttributes redirectAttributes) {
        try {
            Registration registration = registrationRepository.findById(registrationId)
                    .orElseThrow(() -> new EntityNotFoundException("Registration not found"));

            registrationRepository.delete(registration);
            redirectAttributes.addFlashAttribute("successMessage", "Registration deleted successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Failed to delete registration!");
        }
        return "redirect:/admin/registrations";
    }
}