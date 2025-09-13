package com.workout.feed.dto;

import com.workout.feed.domain.Feed;
import com.workout.gym.domain.Gym;
import com.workout.member.domain.Member;
import org.springframework.web.multipart.MultipartFile;

public record FeedCreateRequest(
    MultipartFile image // 이미지 파일
) {
  public Feed toEntity(Member member, String imageUrl) {
    return Feed.builder()
        .member(member)
        .imageUrl(imageUrl)
        .build();
  }
}
