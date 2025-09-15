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
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

  private final MemberService memberService;
  private final FeedRepository feedRepository;
  private final FileService fileService;
  private final LikeRepository likeRepository;
  private final CommentRepository commentRepository;

  public FeedService(MemberService memberService,
      FeedRepository feedRepository, FileService fileService,
      LikeRepository likeRepository, CommentRepository commentRepository) {
    this.memberService = memberService;
    this.feedRepository = feedRepository;
    this.fileService = fileService;
    this.likeRepository = likeRepository;
    this.commentRepository = commentRepository;
  }

  public List<FeedGridResponse> getFeedsForGrid(Long gymId, Long lastFeedId, Long firstFeedId,
      int size) {

    // 파라미터 유효성 검증: 두 커서가 동시에 들어오면 안 됨
    if (lastFeedId != null && firstFeedId != null) {
      throw new RestApiException(FeedErrorCode.INVALID_PARAMETER);
    }

    List<Feed> feeds;
    if (firstFeedId != null) {
      // 최신 피드 조회
      feeds = feedRepository.findNewerFeedsByGymIdWithCursor(gymId, firstFeedId);
    } else {
      Pageable pageable = PageRequest.of(0, size);
      if (lastFeedId == null) {
        // 최초 로딩
        feeds = feedRepository.findByGymIdFirstPage(gymId, pageable);
      } else {
        // 과거 피드 조회
        feeds = feedRepository.findOlderFeedsByGymIdWithCursor(gymId, lastFeedId, pageable);
      }
    }
    return feeds.stream().map(FeedGridResponse::from).toList();
  }

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
    return feedRepository.save(feed).getId();
  }

  @Transactional
  public void deleteFeed(Long feedId, Long userId) {
    Feed feed = feedRepository.findById(feedId)
        .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));
    if (!feed.getMember().getId().equals(userId)) {
      throw new RestApiException(FeedErrorCode.NOT_AUTHORITY);
    }
    likeRepository.deleteAllByFeedId(feedId);
    commentRepository.deleteAllByFeedId(feedId);
    feedRepository.delete(feed);
    fileService.deletePhysicalFile(feed.getImageUrl());
  }
}
