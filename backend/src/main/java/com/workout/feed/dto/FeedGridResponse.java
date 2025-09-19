package com.workout.feed.dto;

import com.workout.feed.domain.Feed;

public record FeedGridResponse(
    Long feedId,
    String imageUrl,
    String authorUsername,
    String authorProfileImageUrl
) {

  public static FeedGridResponse from(Feed feed) {
    return new FeedGridResponse(feed.getId(), feed.getImageUrl(), feed.getMember().getName(),
        feed.getMember().getProfileImageUri());
  }
}