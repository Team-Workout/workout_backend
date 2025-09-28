package com.workout.feed.dto;

import com.workout.feed.domain.Feed;

public record FeedSummaryResponse(
    Long feedId,
    String imageUrl,
    String authorUsername,
    String authorProfileImageUrl,
    Long likeCount,
    Long commentCount,
    boolean isLiked
) {
  public static FeedSummaryResponse of(Feed feed, Long likeCount, Long commentCount, boolean isLiked) {
    return new FeedSummaryResponse(
        feed.getId(),
        feed.getImageUrl(),
        feed.getMember().getName(),
        feed.getMember().getProfileImageUri(),
        likeCount,
        commentCount,
        isLiked
    );
  }

  public static FeedSummaryResponse of(FeedSummaryContent feedSummaryContent, Long likeCount, Long commentCount, boolean isLiked) {
    return new FeedSummaryResponse(
        feedSummaryContent.feedId,
        feedSummaryContent.imageUrl,
        feedSummaryContent.authorUsername,
        feedSummaryContent.authorProfileImageUrl,
        likeCount,
        commentCount,
        isLiked
    );
  }

  public record FeedSummaryContent(
      Long feedId,
      String imageUrl,
      String authorUsername,
      String authorProfileImageUrl
  ) {

  }
}