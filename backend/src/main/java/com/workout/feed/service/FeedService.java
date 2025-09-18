package com.workout.feed.service;

import com.workout.feed.domain.Feed;
import com.workout.feed.domain.LikeType;
import com.workout.feed.dto.FeedCreateRequest;
import com.workout.feed.dto.FeedGridResponse;
import com.workout.feed.dto.FeedSummaryResponse;
import com.workout.feed.repository.CommentRepository;
import com.workout.feed.repository.FeedRepository;
import com.workout.feed.repository.LikeRepository;
import com.workout.global.config.CacheInvalidationPublisher;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.FeedErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.utils.dto.FileResponse;
import com.workout.utils.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeedService {
  private final MemberService memberService;
  private final FeedRepository feedRepository;
  private final FileService fileService;
  private final LikeRepository likeRepository;
  private final CommentRepository commentRepository;
  private final FeedCacheService feedCacheService;
  private final CacheInvalidationPublisher cacheInvalidationPublisher;

  public List<FeedGridResponse> getFeedsForGrid(Long gymId, Long lastFeedId, Long firstFeedId, int size) {
    return feedCacheService.getFeedsForGrid(gymId, lastFeedId, firstFeedId, size);
  }

  @Cacheable(value = "feedSummary", key = "#feedId")
  public FeedSummaryResponse getFeedSummary(Long feedId) {
    Feed feed = feedRepository.findByIdWithMember(feedId)
        .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));
    Long likeCount = likeRepository.countByTargetTypeAndTargetId(LikeType.FEED, feedId);
    Long commentCount = commentRepository.countByFeedId(feedId);
    return FeedSummaryResponse.of(feed, likeCount, commentCount);
  }

  @Transactional
  public Long createFeed(FeedCreateRequest request, Long userId) {
    Member member = memberService.findById(userId);
    FileResponse savedFile = fileService.uploadFeedImages(request.image(), member);
    Feed feed = request.toEntity(member, savedFile.getFileUrl());
    feedRepository.save(feed);

    feedCacheService.addFeedToCache(feed);
    return feed.getId();
  }

  @Transactional
  public void deleteFeed(Long feedId, Long userId) {
    Feed feed = feedRepository.findByIdWithMember(feedId)
        .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));
    if (!feed.getMember().getId().equals(userId)) {
      throw new RestApiException(FeedErrorCode.NOT_AUTHORITY);
    }

    feedCacheService.removeFeedFromCache(feed);
    likeRepository.deleteAllByTargetTypeAndTargetId(LikeType.FEED, feedId);
    feedRepository.delete(feed);
    fileService.deletePhysicalFile(feed.getImageUrl());

    cacheInvalidationPublisher.publish("feedSummary", String.valueOf(feedId));
  }
}