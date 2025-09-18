package com.workout.notification.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import com.workout.notification.service.FcmService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.workout.member.service.MemberService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/fcm")
@Tag(name = "FCM", description = "FCM 토큰 관련")
@RequiredArgsConstructor
public class FCMController {

  private final MemberService memberService;

  // [추가] 알림 발송을 위한 FcmService 주입
  private final FcmService fcmService;

  // [추가] 회원 이름 조회를 위한 MemberRepository 주입
  private final MemberRepository memberRepository;

  @PostMapping("/me/token")
  public ResponseEntity<ApiResponse<Void>> updateFCMToken(
      @RequestBody String fcmToken,
      @AuthenticationPrincipal UserPrincipal userPrincipal
  ) {
    Long userId = userPrincipal.getUserId();

    // 1. (기존 로직) FCM 토큰을 DB에 저장합니다.
    memberService.updateFcmToken(userId, fcmToken);

    // 2. 알림 메시지에 사용할 회원 이름을 조회합니다.
    // (실제 운영 코드에서는 Optional 처리가 필요하지만, 테스트를 위해 간단히 구현)
    Member member = memberRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("테스트 실패: 회원을 찾을 수 없음")); // 혹은 적절한 예외 처리

    // 3. (신규 로직) 토큰 저장 직후, FcmService를 호출하여 환영 메시지를 즉시 발송합니다.
    fcmService.sendNotification(
        fcmToken, // 방금 클라이언트로부터 받은 바로 그 토큰
        "🎉 환영합니다!", // 알림 제목
        member.getName() + "님, FCM 테스트 성공입니다." // 알림 본문
    );

    return ResponseEntity.ok(ApiResponse.empty());
  }
}