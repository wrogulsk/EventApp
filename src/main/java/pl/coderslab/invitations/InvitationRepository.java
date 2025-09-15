package pl.coderslab.invitations;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {

    List<Invitation> findByEventIdOrderBySentAtDesc(Long eventId);

    List<Invitation> findByUserIdOrderBySentAtDesc(Long userId);

    List<Invitation> findByEmailOrderBySentAtDesc(String email);

    List<Invitation> findByEventIdAndStatusOrderBySentAtDesc(Long eventId, InvitationStatus status);

    List<Invitation> findByUserIdAndStatusOrderBySentAtDesc(Long userId, InvitationStatus status);

    Optional<Invitation> findByEventIdAndEmailAndStatusNot(Long eventId, String email, InvitationStatus status);

    long countByEventId(Long eventId);

    long countByEventIdAndStatus(Long eventId, InvitationStatus status);

    List<Invitation> findBySentAtBeforeAndStatus(LocalDateTime cutoffDate, InvitationStatus status);

    boolean existsByEventIdAndEmail(Long eventId, String email);
}
