package pl.coderslab.notifications;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.coderslab.users.User;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<Notification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

    long countByUserIdAndIsReadFalse(Long userId);

    void deleteByCreatedAtBeforeAndIsReadTrue(LocalDateTime cutoffDate);

    @Query("SELECT DISTINCT r.user FROM Registration r WHERE r.event.id = :eventId AND r.status = 'CONFIRMED'")
    List<User> findRegisteredUsersForEvent(@Param("eventId") Long eventId);
}
