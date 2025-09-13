package com.workout.feed.controller;

import com.workout.feed.dto.FeedResponse;
import com.workout.global.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "오운완 피드 (Feed)", description = "오운완 관련 피드 api")
@RestController
@RequestMapping("/api/feed")
public class FeedController {

  //region Feed
  /**
   * 헬스장 별로 피드 조회
   */
  @GetMapping("{gymId}")
  public ResponseEntity<ApiResponse<List<FeedResponse>>> getFeedByGymId(

  ) {

  }

  /**
   * 피드 작성
   */
  @PostMapping()
  public ResponseEntity<ApiResponse<Long> createFeed(

  ) {

  }

  @DeleteMapping()
  public ResponseEntity<ApiResponse<Long> deleteFeed(

  ) {

  }
  //endregion

  //region Like
  /**
   * 피드 좋아요
   */
  @PostMapping()
  public ResponseEntity<ApiResponse<Long> setlikeStatus(

  ) {

  }
  //endregion

  //region Comment
  /**
   * 피드 댓글
   */
  @PostMapping()
  public ResponseEntity<ApiResponse<Long> createComment(

  ) {

  }

  @PatchMapping()
  public ResponseEntity<ApiResponse<Long> updateComment(

  ) {

  }

  @DeleteMapping()
  public ResponseEntity<ApiResponse<Long> deleteComment(

  ) {

  }
  //endregion
}
