package com.workout.feed.service;

import com.workout.feed.domain.Like;
import com.workout.feed.domain.LikeType;
import com.workout.feed.repository.LikeRepository;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class LikeService {
  private final LikeRepository likeRepository;
  private final MemberService memberService;
  private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate 직접 주입

  private static final String FEED_LIKE_COUNT_KEY_PREFIX = "counts:like:feed:";

  @Transactional
  public void toggleLike(Long userId, LikeType targetType, Long targetId) {
    if (targetType != LikeType.FEED) return;

    final String likeCountKey = FEED_LIKE_COUNT_KEY_PREFIX + targetId;

    likeRepository.findByMemberIdAndTargetTypeAndTargetId(userId, targetType, targetId)
        .ifPresentOrElse(
            like -> { // 좋아요 취소
              likeRepository.delete(like);
              TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                  redisTemplate.opsForValue().decrement(likeCountKey);
                }
              });
            },
            () -> { // 좋아요 추가
              Member member = memberService.findById(userId);
              Like newLike = Like.builder().member(member).targetType(targetType).targetId(targetId).build();
              likeRepository.save(newLike);
              TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                  redisTemplate.opsForValue().increment(likeCountKey);
                }
              });
            }
        );
  }
}