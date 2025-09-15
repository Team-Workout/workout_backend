package com.workout.feed.service;

import com.workout.feed.domain.Like;
import com.workout.feed.domain.LikeType;
import com.workout.feed.repository.CommentRepository;
import com.workout.feed.repository.FeedRepository;
import com.workout.feed.repository.LikeRepository;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.CommentErrorCode;
import com.workout.global.exception.errorcode.FeedErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LikeService {

  private final LikeRepository likeRepository;
  private final MemberService memberService;
  private final FeedRepository feedRepository;
  private final CommentRepository commentRepository;

  public LikeService(LikeRepository likeRepository, MemberService memberService,
      FeedRepository feedRepository, CommentRepository commentRepository) {
    this.likeRepository = likeRepository;
    this.memberService = memberService;
    this.commentRepository = commentRepository;
    this.feedRepository = feedRepository;
  }

  @Transactional
  public void toggleLike(Long userId, LikeType targetType, Long targetId) {

    if (targetType == LikeType.FEED) {
      if (!feedRepository.existsById(targetId)) {
        throw new RestApiException(FeedErrorCode.NOT_FOUND);
      }
    } else if (targetType == LikeType.COMMENT) {
      if (!commentRepository.existsById(targetId)) {
        throw new RestApiException(CommentErrorCode.NOT_FOUND);
      }
    } else {
      throw new RestApiException(FeedErrorCode.INVALID_PARAMETER);
    }

    likeRepository.findByMemberIdAndTargetTypeAndTargetId(userId, targetType, targetId)
        .ifPresentOrElse(
            likeRepository::delete, // 이미 좋아요 -> 취소
            () -> { // 좋아요가 없으면 -> 추가
              Member member = memberService.findById(userId);
              Like newLike = Like.builder()
                  .member(member)
                  .targetType(targetType)
                  .targetId(targetId)
                  .build();
              likeRepository.save(newLike);
            }
        );
  }
}
