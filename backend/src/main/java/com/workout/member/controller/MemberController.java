package com.workout.member.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.member.dto.MemberSettingsDto;
import com.workout.member.dto.ProfileResponse;
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

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse<ProfileResponse>> getMyInfo(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    ProfileResponse response = ProfileResponse.from(memberService.findById(userId));

    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @PutMapping("/me/settings/privacy")
  public ResponseEntity<ApiResponse<Void>> updatePrivacySettings(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody MemberSettingsDto settingsDto) {

    Long currentUserId = userPrincipal.getUserId();

    memberService.updatePrivacySettings(currentUserId, settingsDto);

    return ResponseEntity.ok(ApiResponse.empty());
  }
}
