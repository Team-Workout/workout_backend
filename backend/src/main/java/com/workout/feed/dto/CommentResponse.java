package com.workout.feed.dto;

import com.workout.feed.domain.Comment;
import java.time.Instant;

public record CommentResponse(
    Long commentId,
    String content,
    String authorUsername,
    String authorProfileImageUrl,
    Instant createdAt
) {
  public static CommentResponse from(Comment comment) {
    return new CommentResponse(
        comment.getId(),
        comment.getContent(),
        comment.getMember().getName(),
        comment.getMember().getProfileImageUri(),
        comment.getCreatedAt()
    );
  }
}