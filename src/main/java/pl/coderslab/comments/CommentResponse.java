package pl.coderslab.comments;

import java.time.LocalDateTime;

public record CommentResponse(
        String content,
        LocalDateTime createdAt,
        Long eventId,
        String eventTitle,
        Long authorId,
        String authorName
) {
    public static CommentResponse fromEntity(Comment comment) {
        return new CommentResponse(
                comment.getContent(),
                comment.getCreatedAt(),
                comment.getEvent() != null ? comment.getEvent().getId() : null,
                comment.getEvent() != null ? comment.getEvent().getTitle() : null,
                comment.getAuthor() != null ? comment.getAuthor().getId() : null,
                comment.getAuthor() != null
                        ? comment.getAuthor().getFirstName() + " " + comment.getAuthor().getLastName()
                        : null
        );
    }
}
