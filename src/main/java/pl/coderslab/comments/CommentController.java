package pl.coderslab.comments;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable Long id) {
        Comment comment = commentService.getCommentById(id);
        return ResponseEntity.ok(CommentResponse.fromEntity(comment));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<CommentResponse>> getCommentsForEvent(@PathVariable Long eventId) {
        List<Comment> comments = commentService.getCommentsForEvent(eventId);
        List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<CommentResponse>> getCommentsByAuthor(@PathVariable Long authorId) {
        List<Comment> comments = commentService.getCommentsByAuthor(authorId);
        List<CommentResponse> responses = comments.stream()
                .map(CommentResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/event/{eventId}/search")
    public ResponseEntity<List<CommentResponse>> searchCommentsInEvent(
            @PathVariable Long eventId,
            @RequestParam String keyword) {
        List<Comment> comments = commentService.searchCommentsInEvent(eventId, keyword);

        List<CommentResponse> responseList = comments.stream()
                .map(CommentResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(responseList);
    }


    @PostMapping("/add")
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

    //UNUSED GET
    @GetMapping("/search")
    public ResponseEntity<List<Comment>> searchComments(@RequestParam String keyword) {
        List<Comment> comments = commentService.searchComments(keyword);
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

}
