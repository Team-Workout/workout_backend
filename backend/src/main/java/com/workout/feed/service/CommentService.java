// src/main/java/com/workout/feed/service/CommentService.java
package com.workout.feed.service;

import com.workout.feed.domain.Comment;
import com.workout.feed.domain.CommentType;
import com.workout.feed.domain.Feed;
import com.workout.feed.dto.CommentCreateRequest;
import com.workout.feed.dto.CommentResponse;
import com.workout.feed.repository.CommentRepository;
import com.workout.feed.repository.FeedRepository;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.CommentErrorCode;
import com.workout.global.exception.errorcode.FeedErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@RequiredArgsConstructor
public class CommentService {

  private static final String FEED_COMMENT_COUNT_KEY_PREFIX = "counts:comment:feed:";
  private final CommentRepository commentRepository;
  private final MemberService memberService;
  private final FeedRepository feedRepository;
  private final RedisTemplate<String, Object> redisTemplate;


  public Page<CommentResponse> getComments(Long feedId, Pageable pageable) {
    Page<Comment> parentComments = commentRepository.findByFeedIdAndParentIsNull(feedId, pageable);

    Page<CommentResponse> responsePage = parentComments.map(CommentResponse::from);

    List<Long> parentIds = parentComments.getContent().stream()
        .map(Comment::getId)
        .collect(Collectors.toList());

    if (!parentIds.isEmpty()) {
      List<Comment> replies = commentRepository.findRepliesByParentIds(parentIds);

      Map<Long, List<CommentResponse>> repliesByParentId = replies.stream()
          .collect(Collectors.groupingBy(
              reply -> reply.getParent().getId(),
              Collectors.mapping(CommentResponse::from, Collectors.toList())
          ));

      responsePage.getContent().forEach(parentDto -> {
        if (repliesByParentId.containsKey(parentDto.getCommentId())) {
          parentDto.setReplies(repliesByParentId.get(parentDto.getCommentId()));
        }
      });
    }

    return responsePage;
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

    Comment comment = Comment.builder().feed(feed).member(member).content(request.content())
        .parent(parent).build();
    Comment savedComment = commentRepository.save(comment);
    TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
      @Override
      public void afterCommit() {
        redisTemplate.opsForValue().increment(FEED_COMMENT_COUNT_KEY_PREFIX + feed.getId());
      }
    });
    return savedComment.getId();
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = findCommentById(commentId);
    if (!comment.getMember().getId().equals(userId)) {
      throw new RestApiException(CommentErrorCode.NOT_AUTHORITY);
    }

    Long feedId = comment.getFeed().getId();
    long deletedCount = deleteCommentAndChildren(comment);

    if (deletedCount > 0) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          redisTemplate.opsForValue()
              .decrement(FEED_COMMENT_COUNT_KEY_PREFIX + feedId, deletedCount);
        }
      });
    }
  }

  private Comment findCommentById(Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new RestApiException(CommentErrorCode.NOT_FOUND));
  }

  private long deleteCommentAndChildren(Comment comment) {
    long count = 1;
    List<Comment> children = commentRepository.findByParentId(comment.getId());
    if (children != null && !children.isEmpty()) {
      for (Comment child : children) {
        count += deleteCommentAndChildren(child);
      }
    }
    commentRepository.delete(comment);
    return count;
  }
}