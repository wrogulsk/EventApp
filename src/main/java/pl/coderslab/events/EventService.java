package pl.coderslab.events;

import jakarta.transaction.Transactional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.PathVariable;
import pl.coderslab.events.dto.CreateEventRequest;
import pl.coderslab.events.dto.EventResponse;
import pl.coderslab.events.dto.UpdateEventRequest;
import pl.coderslab.locations.Location;
import pl.coderslab.registrations.Registration;
import pl.coderslab.registrations.RegistrationRepository;
import pl.coderslab.registrations.RegistrationStatus;
import pl.coderslab.users.User;
import pl.coderslab.users.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final RegistrationRepository registrationRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository, RegistrationRepository registrationRepository) {
        this.eventRepository = eventRepository;
        this.registrationRepository = registrationRepository;
        this.userRepository = userRepository;
    }

    public List<Event> getAllEventsAsEntities() {
        return eventRepository.findAll();
    }

    public List<EventResponse> getAllEvents() {
        return eventRepository.findAll().stream()
                .map(event -> new EventResponse(
                        event.getId(),
                        event.getTitle(),
                        event.getOrganizer(),
                        event.getStartAt(),
                        event.getEndAt(),
                        event.getCapacity(),
                        event.getLocation(),
                        event.getTags()
                ))
                .collect(Collectors.toList());
    }

    public Optional<Event> getEventById(Long eventId) {
        return Optional.ofNullable(eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found with id: " + eventId)));

    }

    public List<Event> getEventsByOrganizer(@PathVariable String organizer) {
        return eventRepository.findByOrganizer(organizer);
    }

    public List<Event> getEventByLocation(@PathVariable Location location) {
        return eventRepository.findByLocation(location);
    }

    public List<EventResponse> getEventsByCity(String city) {
        return eventRepository.findByLocationCity(city)
                .stream()
                .map(e -> new EventResponse(
                        e.getId(),
                        e.getTitle(),
                        e.getOrganizer() != null ? e.getOrganizer() : null,
                        e.getStartAt(),
                        e.getEndAt(),
                        e.getCapacity(),
                        e.getLocation(),
                        e.getTags()
                ))
                .toList();
    }

    public List<String> getAllCities() {
        return eventRepository.findAllCities();
    }

    public List<String> getAllCitiesByLocation() {
        return eventRepository.findAllCitiesFromLocations();
    }

    public Long createEvent(CreateEventRequest createEventRequest) {

        Event event = new Event();
        event.setOrganizer(createEventRequest.organizer().trim());
        event.setTitle(createEventRequest.title().trim());
        event.setStartAt(createEventRequest.startAt());
        event.setEndAt(createEventRequest.endAt());
        event.setCapacity(createEventRequest.capacity());
        event.setLocation(createEventRequest.location());
        event.setUser(createEventRequest.user());
        event.setTags(createEventRequest.tags());

        return eventRepository.save(event).getId();
    }

    public Long updateEvent(Long id, UpdateEventRequest updateEventRequest) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return 0L;
        }

        event.setTitle(updateEventRequest.title());
        event.setOrganizer(updateEventRequest.organizer());
        event.setStartAt(updateEventRequest.startAt());
        event.setEndAt(updateEventRequest.endAt());
        event.setCapacity(updateEventRequest.capacity());
        event.setLocation(updateEventRequest.location());
        event.setUser(updateEventRequest.user());
        event.setTags(updateEventRequest.tags());

        return eventRepository.save(event).getId();
    }

    public void deleteEvent(Long id) {
        Event event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            return;
        }
        eventRepository.delete(event);
    }

    public Registration registerUserForEvent(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        Event event = eventRepository.findById(eventId).orElseThrow(() -> new IllegalArgumentException("Event not found"));
        boolean alreadyRegistered = registrationRepository.existsByUserIdAndEventId(userId, eventId);
        if (alreadyRegistered) {
            throw new IllegalStateException("User is already registered for this event");
        }


        if (event.getCapacity() != 0) {
            long currentRegistrations = registrationRepository.countByEventId(eventId);
            if (currentRegistrations >= event.getCapacity()) {
                throw new IllegalStateException("Event is at full capacity");
            }
        }
        Registration registration = new Registration();
        registration.setUser(user);
        registration.setEvent(event);
        registration.setRegisteredAt(LocalDateTime.now());
        registration.setStatus(RegistrationStatus.CONFIRMED);
        return registrationRepository.save(registration);
    }

    public void unregisterUserFromEvent(Long userId, Long eventId) {
        Registration registration = registrationRepository
                .findByUserIdAndEventId(userId, eventId)
                .orElseThrow(() -> new IllegalArgumentException("Registration not found"));

        registrationRepository.delete(registration);
    }

    public boolean isUserRegisteredForEvent(Long userId, Long eventId) {
        return registrationRepository.existsByUserIdAndEventId(userId, eventId);
    }

    public List<Registration> getEventRegistrations(Long eventId) {
        return registrationRepository.findByEventId(eventId);
    }

    public List<Registration> getUserRegistrations(Long userId) {
        return registrationRepository.findByUserId(userId);
    }

    public List<Event> getPublicEvents() {
        return eventRepository.findByStartAtAfter(LocalDateTime.now());
    }

    public List<Event> getEventsRegistrationsByUserId(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        return eventRepository.findEventsByRegistrations(user.getRegistrations());
    }

    public List<Event> findEventsWithFilters(String search, Long locationId, Long tagId, String sortBy) {
        List<Event> events = eventRepository.findEventsWithFiltersRaw(search, locationId, tagId, sortBy);

        // USUWA DUPLIKATY ZACHOWUJĄC KOLEJNOŚC
        return new ArrayList<>(events.stream()
                .collect(Collectors.toMap(
                        Event::getId,
                        Function.identity(),
                        (existing, replacement) -> existing,
                        LinkedHashMap::new))
                .values());
    }

    @PreAuthorize("hasRole('ADMIN') or hasRole('ORGANIZER') or @eventService.isOwner(#event.id, authentication.name)")
    public Event save(Event event) {
        return eventRepository.save(event);
    }

    public long getTotalEventsCount() {
        return eventRepository.count();
    }
}
