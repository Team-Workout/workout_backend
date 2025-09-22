package com.workout.feed.dto;

import com.workout.feed.domain.LikeType;

public record LikeRequest(
    LikeType targetType,
    Long targetId
) {

}
