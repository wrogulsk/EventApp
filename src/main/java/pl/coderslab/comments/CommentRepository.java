package pl.coderslab.comments;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByEventIdOrderByCreatedAtAsc(Long eventId);

    Page<Comment> findByEventId(Long eventId, Pageable pageable);

    List<Comment> findByEventIdOrderByCreatedAtDesc(Long eventId, Pageable pageable);

    List<Comment> findByAuthorIdOrderByCreatedAtDesc(Long authorId);

    List<Comment> findByContentContainingIgnoreCaseOrderByCreatedAtDesc(String keyword);

    List<Comment> findByEventIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(Long eventId, String keyword);

    long countByEventId(Long eventId);

    long countByAuthorId(Long authorId);

    List<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<Comment> findByCreatedAtBefore(LocalDateTime cutoffDate);

    @Query("SELECT c FROM Comment c WHERE c.event.user.id = :organizerId ORDER BY c.createdAt DESC")
    List<Comment> findCommentsFromUserEvents(@Param("organizerId") Long organizerId);

    @Query("SELECT c FROM Comment c WHERE c.event.id = :eventId AND c.createdAt >= :since ORDER BY c.createdAt DESC")
    List<Comment> findRecentCommentsForEvent(@Param("eventId") Long eventId, @Param("since") LocalDateTime since);
}
