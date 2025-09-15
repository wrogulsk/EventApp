package pl.coderslab.events;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.coderslab.events.dto.EventResponse;
import pl.coderslab.locations.Location;
import pl.coderslab.registrations.Registration;
import pl.coderslab.users.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"location", "tags"})
    List<Event> findAll();

    @EntityGraph(attributePaths = {"location", "tags"})
    Optional<Event> findById(Long id);

    public List<Event> findByOrganizer(String organizer);

    public List<Event> findByStartAtAfter(LocalDateTime date);

    List<Event> findByLocation(Location location);

    public List<Event> findEventsByRegistrations(Set<Registration> registrations);

    @EntityGraph(attributePaths = {"location", "tags", "organizer"})
    List<Event> findByLocationCity(String city);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.registrations")
    List<Event> findAllWithRegistrations();

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.registrations WHERE e.id = :id")
    Optional<Event> findByIdWithRegistrations(@Param("id") Long id);

    @Query("SELECT DISTINCT e.location.city FROM Event e WHERE e.location.city IS NOT NULL ORDER BY e.location.city")
    List<String> findAllCities();

    @Query("SELECT DISTINCT l.city FROM Location l WHERE l.city IS NOT NULL ORDER BY l.city")
    List<String> findAllCitiesFromLocations();

    @Query("SELECT e FROM Event e " +
            "LEFT JOIN FETCH e.location l " +
            "LEFT JOIN FETCH e.tags t " +
            "WHERE (:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:locationId IS NULL OR e.location.id = :locationId) " +
            "AND (:tagId IS NULL OR :tagId IN (SELECT tag.id FROM e.tags tag)) " +
            "ORDER BY " +
            "CASE WHEN :sortBy = 'title' THEN e.title END ASC, " +
            "CASE WHEN :sortBy = 'organizer' THEN e.organizer END ASC, " +
            "CASE WHEN :sortBy = 'capacity' THEN e.capacity END ASC, " +
            "CASE WHEN :sortBy = 'location' THEN e.location.name END ASC, " +
            "CASE WHEN :sortBy = 'startAt' OR :sortBy IS NULL THEN e.startAt END ASC")
    List<Event> findEventsWithFiltersRaw(
            @Param("search") String search,
            @Param("locationId") Long locationId,
            @Param("tagId") Long tagId,
            @Param("sortBy") String sortBy
    );

}
