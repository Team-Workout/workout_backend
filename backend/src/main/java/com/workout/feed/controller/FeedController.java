package com.workout.feed.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.feed.dto.CommentCreateRequest;
import com.workout.feed.dto.CommentResponse;
import com.workout.feed.dto.FeedCreateRequest;
import com.workout.feed.dto.FeedGridResponse;
import com.workout.feed.dto.FeedSummaryResponse;
import com.workout.feed.dto.LikeRequest;
import com.workout.feed.service.CommentService;
import com.workout.feed.service.FeedService;
import com.workout.feed.service.LikeService;
import com.workout.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "오운완 피드 (Feed)", description = "오운완 관련 피드 api")
@RestController
@RequestMapping("/api/feeds")
@RequiredArgsConstructor
public class FeedController {

  private final FeedService feedService;
  private final LikeService likeService;
  private final CommentService commentService;

  //region Feed
  @Operation(summary = "헬스장 별 피드 목록 조회 (커서 기반)", description = "커서(ID)를 기반으로 피드 목록을 조회합니다.")
  @GetMapping
  public ResponseEntity<ApiResponse<List<FeedGridResponse>>> getFeeds(
      @Parameter(description = "헬스장 ID") @RequestParam Long gymId,
      @Parameter(description = "이 피드 ID보다 오래된 게시물을 조회 (아래로 스크롤)") @RequestParam(required = false) Long lastFeedId,
      @Parameter(description = "이 피드 ID보다 최신 게시물을 조회 (위로 새로고침)") @RequestParam(required = false) Long firstFeedId,
      @Parameter(description = "한 번에 가져올 개수") @RequestParam(defaultValue = "20") int size) {

    List<FeedGridResponse> feeds = feedService.getFeedsForGrid(gymId, lastFeedId, firstFeedId,
        size);
    return ResponseEntity.ok(ApiResponse.of(feeds));
  }

  @Operation(summary = "피드 요약 조회", description = "피드의 좋야요, 댓글 수 등을 조회")
  @GetMapping
  public ResponseEntity<ApiResponse<FeedSummaryResponse>> getFeedSummary(
      @Parameter(description = "피드 ID") @RequestParam Long feedId) {

    FeedSummaryResponse feeds = feedService.getFeedSummary(feedId);
    return ResponseEntity.ok(ApiResponse.of(feeds));
  }

  @Operation(summary = "피드 작성", description = "이미지 파일을 업로드하여 새로운 피드를 작성합니다.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ResponseEntity<ApiResponse<Long>> createFeed(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "이미지 파일") @RequestPart("image") MultipartFile image) {
    FeedCreateRequest request = new FeedCreateRequest(image);
    Long userId = userPrincipal.getUserId();
    Long feedId = feedService.createFeed(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(feedId));
  }

  @Operation(summary = "피드 삭제")
  @DeleteMapping("/{feedId}")
  public ResponseEntity<ApiResponse<Void>> deleteFeed(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "피드 ID") @PathVariable Long feedId) {
    Long userId = userPrincipal.getUserId();
    feedService.deleteFeed(feedId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }
  //endregion

  //region Like
  @Operation(summary = "피드 또는 댓글에 좋아요/좋아요 취소")
  @PostMapping("/like")
  public ResponseEntity<ApiResponse<Void>> toggleLike(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestBody LikeRequest request) {
    Long userId = userPrincipal.getUserId();
    likeService.toggleLike(userId, request.targetType(), request.targetId());
    return ResponseEntity.ok(ApiResponse.empty());
  }
  //endregion

  //region Comment
  @Operation(summary = "특정 피드의 댓글 목록 조회")
  @GetMapping("/{feedId}/comments")
  public ResponseEntity<ApiResponse<List<CommentResponse>>> getComments(
      @Parameter(description = "피드 ID") @PathVariable Long feedId,
      Pageable pageable) {
    Page<CommentResponse> comments = commentService.getComments(feedId, pageable);
    return ResponseEntity.ok(ApiResponse.of(comments));
  }

  @Operation(summary = "댓글 또는 대댓글 작성")
  @PostMapping("/comments")
  public ResponseEntity<ApiResponse<Long>> createComment(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @RequestBody CommentCreateRequest request) {
    Long userId = userPrincipal.getUserId();
    Long commentId = commentService.createComment(request, userId);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(commentId));
  }

  @Operation(summary = "댓글 삭제")
  @DeleteMapping("{commentId}/comments")
  public ResponseEntity<ApiResponse<Void>> deleteComment(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Parameter(description = "댓글 ID") @PathVariable Long commentId) {
    Long userId = userPrincipal.getUserId();
    commentService.deleteComment(commentId, userId);
    return ResponseEntity.ok(ApiResponse.empty());
  }
  //endregion
}