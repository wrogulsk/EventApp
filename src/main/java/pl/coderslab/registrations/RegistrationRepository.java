package pl.coderslab.registrations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.coderslab.events.Event;
import pl.coderslab.users.User;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    boolean existsByUserAndEvent(User user, Event event);

    boolean existsByUserIdAndEventId(Long userId, Long eventId);

    boolean existsByUserIdAndEventIdAndStatus(Long userId, Long eventId, RegistrationStatus status);

    List<Registration> findByUserIdAndStatus(Long userId, RegistrationStatus status);

    List<Registration> findByEventIdAndStatus(Long eventId, RegistrationStatus status);

    long countByEventAndStatus(Event event, RegistrationStatus status);

    long countByEventId(Long eventId);

    Optional<Registration> findByUserIdAndEventId(Long userId, Long eventId);

    List<Registration> findByEventId(Long eventId);

    List<Registration> findByUserId(Long userId);

    @Query("SELECT r FROM Registration r JOIN FETCH r.user WHERE r.event.id = :eventId")
    List<Registration> findByEventIdWithUser(@Param("eventId") Long eventId);

    Optional<Registration> findByEventIdAndUserId(Long eventId, Long userId);


}
