package pl.coderslab.events;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.coderslab.events.dto.CreateEventRequest;
import pl.coderslab.events.dto.EventResponse;
import pl.coderslab.locations.LocationService;
import pl.coderslab.registrations.Registration;
import pl.coderslab.tags.Tag;
import pl.coderslab.tags.TagService;
import pl.coderslab.users.UserService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("/ui/events")
public class EventPageController {

    private final EventService eventService;
    private final UserService userService;
    private final LocationService locationService;
    private final TagService tagService;

    public EventPageController(EventService eventService, UserService userService, LocationService locationService, TagService tagService) {
        this.eventService = eventService;
        this.userService = userService;
        this.locationService = locationService;
        this.tagService = tagService;
    }

    @GetMapping
    public String list(Model model) {
        List<EventResponse> events = eventService.getAllEvents();
        model.addAttribute("events", events);
        return "events/list";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        Event event = eventService.getEventById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        List<Registration> registrations = eventService.getEventRegistrations(id);
        Long currentUserId = 1L;
        boolean isUserRegistered = eventService.isUserRegisteredForEvent(currentUserId, id);

        model.addAttribute("e", event);
        model.addAttribute("registrationCount", registrations.size());
        model.addAttribute("isUserRegistered", isUserRegistered);
        model.addAttribute("registrations", registrations);

        return "events/detail";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        CreateEventRequest emptyRequest = new CreateEventRequest(
                null, null, null, null, null, null, null, new HashSet<>()
        );

        model.addAttribute("eventRequest", emptyRequest);
        model.addAttribute("locations", locationService.getAllLocations());
        model.addAttribute("users", userService.getUsers());
        model.addAttribute("allTags", tagService.getAllTags());
        return "events/create";
    }

    @PostMapping("/create")
    public String createEvent(@ModelAttribute CreateEventRequest event,
                              @RequestParam(value = "tagIds", required = false) List<Long> tagIds,
                              RedirectAttributes redirectAttributes) {
        try {
            Set<Tag> selectedTags = new HashSet<>();
            if (tagIds != null && !tagIds.isEmpty()) {
                for (Long tagId : tagIds) {
                    Tag tag = tagService.findById(tagId);
                    if (tag != null) {
                        selectedTags.add(tag);
                    }
                }
            }

            CreateEventRequest requestWithTags = new CreateEventRequest(
                    event.title(),
                    event.organizer(),
                    event.startAt(),
                    event.endAt(),
                    event.capacity(),
                    event.location(),
                    event.user(),
                    selectedTags
            );

            Long eventId = eventService.createEvent(requestWithTags);
            redirectAttributes.addFlashAttribute("success", "Event created successfully!");
            return "redirect:/ui/events/" + eventId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create event: " + e.getMessage());
            return "redirect:/ui/events/create";
        }
    }

}