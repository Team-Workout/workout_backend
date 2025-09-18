package com.workout.feed.service;

import com.workout.feed.domain.Like;
import com.workout.feed.domain.LikeType;
import com.workout.feed.repository.LikeRepository;
import com.workout.global.config.CacheInvalidationPublisher;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {
  private final LikeRepository likeRepository;
  private final MemberService memberService;
  private final CacheInvalidationPublisher cacheInvalidationPublisher;

  @Transactional
  public void toggleLike(Long userId, LikeType targetType, Long targetId) {
    likeRepository.findByMemberIdAndTargetTypeAndTargetId(userId, targetType, targetId)
        .ifPresentOrElse(likeRepository::delete, () -> {
              Member member = memberService.findById(userId);
              Like newLike = Like.builder().member(member).targetType(targetType).targetId(targetId).build();
              likeRepository.save(newLike);
            }
        );

    if (targetType == LikeType.FEED) {
      cacheInvalidationPublisher.publish("feedSummary", String.valueOf(targetId));
    }
  }
}