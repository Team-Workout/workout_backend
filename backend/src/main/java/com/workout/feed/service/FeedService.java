package com.workout.feed.service;

import com.workout.feed.domain.Feed;
import com.workout.feed.dto.FeedCreateRequest;
import com.workout.feed.dto.FeedResponse;
import com.workout.feed.repository.FeedRepository;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.utils.service.FileService;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class FeedService {
  private final  MemberService memberService;
  private final FeedRepository feedRepository;
  private final FileService fileService;

  public FeedService(MemberService memberService,
      FeedRepository feedRepository, FileService fileService) {
    this.memberService = memberService;
    this.feedRepository = feedRepository;
    this.fileService = fileService;
  }

  //region Feed
  //피드 조회
  List<FeedResponse> getFeed() {

  }

  //피드 생성
  Long createFeed(FeedCreateRequest request, Long userId) {
    Member member = memberService.findById(userId);
    String feedurl =
    Feed newFeed;

    feedRepository.save(newFeed);
  }

  //피드 삭제
  //endregion
  void deleteFeed() {
    //하위 댓글 삭제

    //좋아요 삭제

    //댓글 삭제
  }

  //region Like
  //좋아요 추가

  //좋아요 취소
  //endregion

  //region Post
  //댓글 생성

  //댓글 조회

  //댓글 수정
  //endregion
}
