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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

  private final CommentRepository commentRepository;
  private final MemberService memberService;
  private final FeedRepository feedRepository;

  public CommentService(CommentRepository commentRepository, MemberService memberService,
      FeedRepository feedRepository) {
    this.commentRepository = commentRepository;
    this.memberService = memberService;
    this.feedRepository = feedRepository;
  }

  private Comment findCommentById(Long commentId) {
    return commentRepository.findById(commentId)
        .orElseThrow(() -> new RestApiException(CommentErrorCode.NOT_FOUND));
  }

  public Page<CommentResponse> getComments(Long feedId, Pageable pageable) {
    if (!feedRepository.existsById(feedId)) {
      throw new RestApiException(FeedErrorCode.NOT_FOUND);
    }

    Page<Comment> commentPage = commentRepository.findByFeedIdAndParentIsNull(feedId, pageable);

    return commentPage.map(CommentResponse::from);
  }

  public Long createComment(CommentCreateRequest request, Long userId) {
    Member member = memberService.findById(userId);

    Comment comment;
    if (request.targetType() == CommentType.FEED) {
      Feed feed = feedRepository.findById(request.targetId())
          .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));

      comment = Comment.builder()
          .feed(feed)
          .member(member)
          .content(request.content())
          .parent(null)
          .build();

    } else if (request.targetType() == CommentType.COMMENT) {
      Comment parentComment = findCommentById(request.targetId());

      comment = Comment.builder()
          .feed(parentComment.getFeed())
          .member(member)
          .content(request.content())
          .parent(parentComment)
          .build();
    } else {
      throw new RestApiException(CommentErrorCode.INVALID_PARAMETER);
    }

    return commentRepository.save(comment).getId();
  }

  @Transactional
  public void deleteComment(Long commentId, Long userId) {
    Comment comment = findCommentById(commentId);

    if (!comment.getMember().getId().equals(userId)) {
      throw new RestApiException(CommentErrorCode.NOT_AUTHORITY);
    }

    deleteCommentAndChildren(comment);
  }

  private void deleteCommentAndChildren(Comment comment) {
    List<Comment> children = commentRepository.findByParentId(comment.getId());

    for (Comment child : children) {
      deleteCommentAndChildren(child);
    }

    commentRepository.delete(comment);
  }
}
