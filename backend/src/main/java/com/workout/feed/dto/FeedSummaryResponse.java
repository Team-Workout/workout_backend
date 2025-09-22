package com.workout.feed.dto;

import com.workout.feed.domain.Feed;

public record FeedSummaryResponse(
    Long feedId,
    String imageUrl,
    String authorUsername,
    String authorProfileImageUrl,
    Long likeCount,
    Long commentCount
) {
  public static FeedSummaryResponse of(Feed feed, Long likeCount, Long commentCount) {
    return new FeedSummaryResponse(
        feed.getId(),
        feed.getImageUrl(),
        feed.getMember().getName(),
        feed.getMember().getProfileImageUri(),
        likeCount,
        commentCount
    );
  }

  public static FeedSummaryResponse of(FeedSummaryContent feedSummaryContent, Long likeCount, Long commentCount) {
    return new FeedSummaryResponse(
        feedSummaryContent.feedId,
        feedSummaryContent.imageUrl,
        feedSummaryContent.authorUsername,
        feedSummaryContent.authorProfileImageUrl,
        likeCount,
        commentCount
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