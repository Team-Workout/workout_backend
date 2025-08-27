package com.workout.member.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.member.dto.ProfileResponse;
import com.workout.member.dto.WorkoutLogSettingsDto;
import com.workout.member.service.MemberService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/member")
public class MemberController {

  private final MemberService memberService;

  // MemberService를 주입받는 생성자
  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @PutMapping("/me/settings/workout-log-access") // 리소스 경로를 더 명확하게 지정
  public ResponseEntity<Void> updateWorkoutLogAccessSettings(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody WorkoutLogSettingsDto settingsDto) {

    Long currentUserId = userPrincipal.getUserId();

    if (settingsDto.isOpenWorkoutRecord()) {
      memberService.allowAccessWorkoutLog(currentUserId);
    } else {
      memberService.forbidAccessWorkoutLog(currentUserId);
    }

    return ResponseEntity.noContent().build();
  }

  @GetMapping("/me")
  public ResponseEntity<ProfileResponse> getMyInfo(@AuthenticationPrincipal UserPrincipal userPrincipal) {
    Long userId = userPrincipal.getUserId();
    ProfileResponse response = ProfileResponse.from(memberService.findById(userId));
    return ResponseEntity.ok(response);
  }
}
