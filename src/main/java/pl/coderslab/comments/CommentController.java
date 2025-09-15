package pl.coderslab.comments;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping
    public ResponseEntity<Comment> createComment(
            @RequestParam Long eventId,
            @RequestParam Long authorId,
            @RequestBody Map<String, String> request) {
        String content = request.get("content");
        Comment comment = commentService.createComment(eventId, authorId, content);
        return ResponseEntity.status(HttpStatus.CREATED).body(comment);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Long id,
            @RequestParam Long authorId,
            @RequestBody Map<String, String> request) {
        String content = request.get("content");
        Comment comment = commentService.updateComment(id, authorId, content);
        return ResponseEntity.ok(comment);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long id,
            @RequestParam Long userId) {
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Comment> getComment(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(comment);
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Comment>> getCommentsForEvent(@PathVariable Long eventId) {
        List<Comment> comments = commentService.getCommentsForEvent(eventId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/event/{eventId}/paginated")
    public ResponseEntity<List<Comment>> getCommentsForEventPaginated(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<Comment> comments = commentService.getCommentsForEventPaginated(eventId, page, size);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<Comment>> getCommentsByAuthor(@PathVariable Long authorId) {
        List<Comment> comments = commentService.getCommentsByAuthor(authorId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Comment>> searchComments(@RequestParam String keyword) {
        List<Comment> comments = commentService.searchComments(keyword);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/event/{eventId}/search")
    public ResponseEntity<List<Comment>> searchCommentsInEvent(
            @PathVariable Long eventId,
            @RequestParam String keyword) {
        List<Comment> comments = commentService.searchCommentsInEvent(eventId, keyword);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/event/{eventId}/count")
    public ResponseEntity<Long> getCommentCountForEvent(@PathVariable Long eventId) {
        long count = commentService.getCommentCountForEvent(eventId);
        return ResponseEntity.ok(count);
    }

    @GetMapping("/recent")
    public ResponseEntity<List<Comment>> getRecentComments(@RequestParam(defaultValue = "10") int limit) {
        List<Comment> comments = commentService.getRecentComments(limit);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/event/{eventId}/recent")
    public ResponseEntity<List<Comment>> getRecentCommentsForEvent(
            @PathVariable Long eventId,
            @RequestParam(defaultValue = "5") int limit) {
        List<Comment> comments = commentService.getRecentCommentsForEvent(eventId, limit);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/moderation/{organizerId}")
    public ResponseEntity<List<Comment>> getCommentsForModeration(@PathVariable Long organizerId) {
        List<Comment> comments = commentService.getCommentsForModeration(organizerId);
        return ResponseEntity.ok(comments);
    }

    @DeleteMapping("/{id}/moderate")
    public ResponseEntity<Void> moderateComment(
            @PathVariable Long id,
            @RequestParam Long moderatorId,
            @RequestParam String reason) {
        commentService.moderateComment(id, moderatorId, reason);
        return ResponseEntity.noContent().build();
    }
}
