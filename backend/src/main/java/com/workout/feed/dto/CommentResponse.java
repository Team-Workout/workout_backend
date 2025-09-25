package com.workout.feed.dto;

import com.workout.feed.domain.Comment;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class CommentResponse {

  private final Long commentId;
  private final String content;
  private final String authorUsername;
  private final String authorProfileImageUrl;
  private final Instant createdAt;
  private final Long parentId;
  @Setter
  private List<CommentResponse> replies;

  private CommentResponse(Comment comment) {
    this.commentId = comment.getId();
    this.content = comment.getContent();
    this.authorUsername = comment.getMember().getName();
    this.authorProfileImageUrl = comment.getMember().getProfileImageUri();
    this.createdAt = comment.getCreatedAt();
    this.parentId = (comment.getParent() != null) ? comment.getParent().getId() : null;
    this.replies = Collections.emptyList();
  }

  public static CommentResponse from(Comment comment) {
    return new CommentResponse(comment);
  }
}