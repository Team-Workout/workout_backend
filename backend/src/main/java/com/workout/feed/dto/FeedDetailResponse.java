package com.workout.feed.dto;

import com.workout.feed.domain.Comment;
import com.workout.feed.domain.Feed;
import java.time.Instant;
import java.util.List;

/**
 * 피드 상세 조회를 위한 모든 정보가 포함된 응답 DTO
 */
public record FeedDetailResponse(
    Long feedId,
    String imageUrl,
    String authorUsername,
    String authorProfileImageUrl,
    Long likeCount,
    List<CommentResponse> comments, // 댓글 전체 목록 포함
    Instant createdAt
) {

  public static FeedDetailResponse of(Feed feed, Long likeCount, List<Comment> comments) {
    return new FeedDetailResponse(
        feed.getId(),
        feed.getImageUrl(),
        feed.getMember().getName(),
        feed.getMember().getProfileImageUri(),
        likeCount,
        comments.stream().map(CommentResponse::from).toList(),
        feed.getCreatedAt()
    );
  }
}