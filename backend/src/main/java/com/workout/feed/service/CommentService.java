// src/main/java/com/workout/feed/service/CommentService.java
package com.workout.feed.service;

import com.workout.feed.domain.Comment;
import com.workout.feed.domain.CommentType;
import com.workout.feed.domain.Feed;
import com.workout.feed.dto.CommentCreateRequest;
import com.workout.feed.dto.CommentResponse;
import com.workout.feed.repository.CommentRepository;
import com.workout.feed.repository.FeedRepository;
import com.workout.global.config.CacheInvalidationPublisher;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.CommentErrorCode;
import com.workout.global.exception.errorcode.FeedErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {
  private final CommentRepository commentRepository;
  private final MemberService memberService;
  private final FeedRepository feedRepository;
  private final CacheInvalidationPublisher cacheInvalidationPublisher;

  @Cacheable(value = "comments", key = "#feedId + ':' + #pageable.pageNumber")
  public Page<CommentResponse> getComments(Long feedId, Pageable pageable) {
    return commentRepository.findByFeedIdAndParentIsNull(feedId, pageable).map(CommentResponse::from);
  }

  @Transactional
  public Long createComment(CommentCreateRequest request, Long userId) {
    Member member = memberService.findById(userId);
    Feed feed;
    Comment parent = null;

    if (request.targetType() == CommentType.FEED) {
      feed = feedRepository.findById(request.targetId())
          .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));
    } else if (request.targetType() == CommentType.COMMENT) {
      parent = findCommentById(request.targetId());
      feed = parent.getFeed();
    } else {
      throw new RestApiException(CommentErrorCode.INVALID_PARAMETER);
    }

    Comment comment = Comment.builder().feed(feed).member(member).content(request.content()).parent(parent).build();
    Comment savedComment = commentRepository.save(comment);

    // Invalidate the feed summary (for comment count) and all comment pages for this feed
    cacheInvalidationPublisher.publish("feedSummary", String.valueOf(feed.getId()));
    // This is a simplification. A better approach would be to only evict the first page of comments.
    // For now, let's assume invalidating by feedId is a prefix-based invalidation.
    // However, Spring's default cache manager doesn't support prefix eviction easily.
    // Therefore, invalidating each known page or having a smarter strategy is needed for production.
    // A simple approach is to have L1 cache on `getComments` be very short-lived.
    cacheInvalidationPublisher.publish("comments", String.valueOf(feed.getId()));

    return savedComment.getId();
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = findCommentById(commentId);
    if (!comment.getMember().getId().equals(userId)) {
      throw new RestApiException(CommentErrorCode.NOT_AUTHORITY);
    }

    Long feedId = comment.getFeed().getId();
    deleteCommentAndChildren(comment);

    // Invalidate caches after the transaction
    cacheInvalidationPublisher.publish("feedSummary", String.valueOf(feedId));
    cacheInvalidationPublisher.publish("comments", String.valueOf(feedId));
  }

  private Comment findCommentById(Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new RestApiException(CommentErrorCode.NOT_FOUND));
  }

  private void deleteCommentAndChildren(Comment comment) {
    List<Comment> children = commentRepository.findByParentId(comment.getId());
    if (children != null && !children.isEmpty()) {
      for (Comment child : children) {
        deleteCommentAndChildren(child);
      }
    }
    commentRepository.delete(comment);
  }
}