package pl.coderslab.comments;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.coderslab.events.Event;
import pl.coderslab.events.EventRepository;
import pl.coderslab.notifications.NotificationService;
import pl.coderslab.users.User;
import pl.coderslab.users.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public CommentService(CommentRepository commentRepository,
                          EventRepository eventRepository,
                          UserRepository userRepository,
                          NotificationService notificationService) {
        this.commentRepository = commentRepository;
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }
    // Tworzenie komentarza
    public Comment createComment(Long eventId, Long authorId, String content) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        validateCommentContent(content);

        Comment comment = new Comment();
        comment.setEvent(event);
        comment.setAuthor(author);
        comment.setContent(content.trim());

        Comment savedComment = commentRepository.save(comment);

        // Powiadom organizatora eventu o nowym komentarzu (jeśli to nie on sam)
        if (!event.getUser().getId().equals(authorId)) {
            String message = String.format("Nowy komentarz do Twojego eventu '%s' od %s",
                    event.getTitle(), author.getFirstName());
            notificationService.createNotification(event.getUser().getId(), message, eventId);
        }

        return savedComment;
    }

    // Aktualizacja komentarza
    public Comment updateComment(Long commentId, Long authorId, String newContent) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Sprawdź czy użytkownik może edytować komentarz
        if (!comment.getAuthor().getId().equals(authorId)) {
            throw new SecurityException("You can only edit your own comments");
        }

        validateCommentContent(newContent);

        comment.setContent(newContent.trim());
        return commentRepository.save(comment);
    }

    // Usuwanie komentarza
    public void deleteComment(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));

        // Sprawdź czy użytkownik może usunąć komentarz (autor lub organizator eventu)
        boolean isAuthor = comment.getAuthor().getId().equals(userId);
        boolean isEventOrganizer = comment.getEvent().getUser().getId().equals(userId);

        if (!isAuthor && !isEventOrganizer) {
            throw new SecurityException("You can only delete your own comments or comments from your events");
        }

        commentRepository.delete(comment);
    }

    // Pobieranie komentarzy
    public List<Comment> getCommentsForEvent(Long eventId) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        return commentRepository.findByEventIdOrderByCreatedAtAsc(eventId);
    }

    public List<Comment> getCommentsForEventPaginated(Long eventId, int page, int size) {
        eventRepository.findById(eventId)
                .orElseThrow(() -> new EntityNotFoundException("Event not found"));

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Comment> commentsPage = commentRepository.findByEventId(eventId, pageable);
        return commentsPage.getContent();
    }

    public List<Comment> getCommentsByAuthor(Long authorId) {
        userRepository.findById(authorId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        return commentRepository.findByAuthorIdOrderByCreatedAtDesc(authorId);
    }

    public Comment getCommentById(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new EntityNotFoundException("Comment not found"));
    }

    // Wyszukiwanie komentarzy
    public List<Comment> searchComments(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return new ArrayList<>();
        }

        return commentRepository.findByContentContainingIgnoreCaseOrderByCreatedAtDesc(keyword.trim());
    }

    public List<Comment> searchCommentsInEvent(Long eventId, String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getCommentsForEvent(eventId);
        }

        return commentRepository.findByEventIdAndContentContainingIgnoreCaseOrderByCreatedAtDesc(eventId, keyword.trim());
    }

    // Statystyki
    public long getCommentCountForEvent(Long eventId) {
        return commentRepository.countByEventId(eventId);
    }

    public long getCommentCountByAuthor(Long authorId) {
        return commentRepository.countByAuthorId(authorId);
    }

    public List<Comment> getRecentComments(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return commentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<Comment> getRecentCommentsForEvent(Long eventId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return commentRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
    }

    // Moderacja
    public List<Comment> getCommentsForModeration(Long organizerId) {
        // Pobierz komentarze z eventów organizowanych przez danego użytkownika
        return commentRepository.findCommentsFromUserEvents(organizerId);
    }

    public void moderateComment(Long commentId, Long moderatorId, String reason) {
        Comment comment = getCommentById(commentId);

        // Sprawdź czy moderator może moderować ten komentarz (organizator eventu)
        if (!comment.getEvent().getUser().getId().equals(moderatorId)) {
            throw new SecurityException("You can only moderate comments from your events");
        }

        // Powiadom autora komentarza o moderacji
        String message = String.format("Twój komentarz do eventu '%s' został usunięty. Powód: %s",
                comment.getEvent().getTitle(), reason);
        notificationService.createNotification(comment.getAuthor().getId(), message, comment.getEvent().getId());

        commentRepository.delete(comment);
    }

    // Walidacja
    private void validateCommentContent(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("Comment content cannot be empty");
        }

        if (content.trim().length() < 3) {
            throw new IllegalArgumentException("Comment must be at least 3 characters long");
        }

        if (content.length() > 1000) {
            throw new IllegalArgumentException("Comment cannot exceed 1000 characters");
        }

        // Podstawowa filtracja wulgaryzmów (można rozszerzyć)
        String[] bannedWords = {"spam", "fake", "scam"}; // przykład
        String lowerContent = content.toLowerCase();
        for (String word : bannedWords) {
            if (lowerContent.contains(word)) {
                throw new IllegalArgumentException("Comment contains inappropriate content");
            }
        }
    }

    // Automatyczne czyszczenie starych komentarzy (opcjonalne)
    @Scheduled(cron = "0 0 3 * * ?") // Codziennie o 3:00
    public void cleanupOldComments() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusMonths(12); // 12 miesięcy
        List<Comment> oldComments = commentRepository.findByCreatedAtBefore(cutoffDate);

        if (!oldComments.isEmpty()) {
            commentRepository.deleteAll(oldComments);
            System.out.println("Cleaned up " + oldComments.size() + " old comments");
        }
    }
}
