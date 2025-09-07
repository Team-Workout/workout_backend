package com.workout.member.controller;

import com.workout.auth.domain.UserPrincipal;
import com.workout.global.dto.ApiResponse;
import com.workout.member.dto.MemberSettingsDto;
import com.workout.member.dto.ProfileResponse;
import com.workout.member.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "회원 (Member)", description = "로그인한 일반 회원(MEMBER) 본인 정보 관련 API")
@RestController
@RequestMapping("/api/member")
public class MemberController {

  private final MemberService memberService;

  public MemberController(MemberService memberService) {
    this.memberService = memberService;
  }

  @Operation(summary = "내 정보 조회 (본인)",
      description = "로그인한 회원 본인의 프로필 정보(이름, 이메일, 공개 설정 등)를 조회합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "내 정보 조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "회원 정보를 찾을 수 없음")
  })
  @GetMapping("/me")
  public ResponseEntity<ApiResponse<ProfileResponse>> getMyInfo(
      @AuthenticationPrincipal UserPrincipal userPrincipal) {

    Long userId = userPrincipal.getUserId();
    ProfileResponse response = ProfileResponse.from(memberService.findById(userId));

    return ResponseEntity.ok(ApiResponse.of(response));
  }

  @Operation(summary = "내 정보 공개 설정 변경 (본인)",
      description = "운동 기록, 인바디, 바디 이미지의 공개 범위를 설정합니다.",
      security = @SecurityRequirement(name = "cookieAuth"))
  @ApiResponses(value = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "설정 변경 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "입력값 오류 (모든 값은 NotNull)"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "인증되지 않은 사용자")
  })
  @PutMapping("/me/settings/privacy")
  public ResponseEntity<ApiResponse<Void>> updatePrivacySettings(
      @AuthenticationPrincipal UserPrincipal userPrincipal,
      @Valid @RequestBody MemberSettingsDto settingsDto) {

    Long currentUserId = userPrincipal.getUserId();

    memberService.updatePrivacySettings(currentUserId, settingsDto);

    return ResponseEntity.ok(ApiResponse.empty());
  }
}
