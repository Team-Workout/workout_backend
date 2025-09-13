package com.workout.feed.dto;

import com.workout.feed.domain.Comment;
import com.workout.feed.domain.Feed;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record FeedResponse(
    Long feedId,
    String content,
    String imageUrl,
    String authorUsername, // 작성자 이름
    Long likeCount,        // 좋아요 수
    List<CommentResponse> comments, // 댓글 목록
    LocalDateTime createdAt
) {

  // 엔티티를 DTO로 변환하는 정적 팩토리 메소드
  public static FeedResponse from(Feed feed) {
    return new FeedResponse(
        feed.getId(),
        feed.getImageUrl(),
        feed.getMember().getName(), // Member 엔티티에서 사용자 이름 가져오기
        (long) feed.getLikes().size(),  // 좋아요 목록의 크기로 카운트
        feed.getComments().stream()
            .map(CommentResponse::from)
            .collect(Collectors.toList()),
        feed.getCreatedAt()
    );
  }

  // 내부 record로 댓글 응답 DTO 정의
  public record CommentResponse(
      Long commentId,
      String content,
      String authorUsername,
      LocalDateTime createdAt
  ) {
    public static CommentResponse from(Comment comment) {
      return new CommentResponse(
          comment.getId(),
          comment.getContent(),
          comment.getMember().getUsername(),
          comment.getCreatedAt()
      );
    }
  }
}
