package com.workout.feed.dto;

import com.workout.feed.domain.Feed;

public record FeedGridResponse (
    Long feedId,
    String imageUrl
) {
  public static FeedGridResponse from(Feed feed) {
    return new FeedGridResponse(feed.getId(), feed.getImageUrl());
  }
}