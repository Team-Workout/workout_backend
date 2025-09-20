package com.workout.feed.service;

import com.workout.feed.domain.Feed;
import com.workout.feed.domain.LikeType;
import com.workout.feed.dto.FeedCreateRequest;
import com.workout.feed.dto.FeedGridResponse;
import com.workout.feed.dto.FeedSummaryResponse;
import com.workout.feed.repository.CommentRepository;
import com.workout.feed.repository.FeedRepository;
import com.workout.feed.repository.LikeRepository;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.FeedErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class FeedService {

  private final MemberService memberService;
  private final FeedRepository feedRepository;
  private final FileService fileService;
  private final LikeRepository likeRepository;
  private final FeedCacheService feedCacheService;
  private final CommentRepository commentRepository;

  public List<FeedGridResponse> getFeedsForGrid(Long gymId, Long lastFeedId, Long firstFeedId,
      int size) {
    return feedCacheService.getFeedsForGrid(gymId, lastFeedId, firstFeedId, size);
  }

  public FeedSummaryResponse getFeedSummary(Long feedId) {
    return feedCacheService.getFeedSummary(feedId);
  }

  @Transactional
  public Long createFeed(FeedCreateRequest request, Long userId) {
    Member member = memberService.findById(userId);
    FileResponse savedFile = fileService.uploadFeedImages(request.image(), member);
    Feed feed = request.toEntity(member, savedFile.getFileUrl());
    feedRepository.save(feed);

    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        feedCacheService.addFeedToCache(feed);
      }
    });
    return feed.getId();
  }

  @Transactional
  public void deleteFeed(Long feedId, Long userId) {
    Feed feed = feedRepository.findByIdWithMember(feedId)
        .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));
    if (!feed.getMember().getId().equals(userId)) {
      throw new RestApiException(FeedErrorCode.NOT_AUTHORITY);
    }

    // [수정] DB 삭제 로직: 좋아요, 댓글, 피드 순으로 삭제
    likeRepository.deleteAllByTargetTypeAndTargetId(LikeType.FEED, feedId);
    commentRepository.deleteAllByFeedId(feedId); // 피드에 달린 모든 댓글 삭제
    feedRepository.delete(feed);
    fileService.deletePhysicalFile(feed.getImageUrl());

    // DB 트랜잭션이 성공적으로 커밋된 후에만 캐시를 삭제하여 데이터 정합성 보장
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        feedCacheService.removeFeedFromCache(feed);
      }
    });
  }
}