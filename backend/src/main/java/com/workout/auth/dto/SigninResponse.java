package com.workout.auth.dto;

import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답 DTO")
public record SigninResponse(
    @Schema(description = "로그인한 사용자 ID", example = "1")
    Long id,
    @Schema(description = "사용자 이름", example = "김철수")
    String name,
    @Schema(description = "소속 헬스장 ID", example = "1")
    Long gymId,
    @Schema(description = "사용자 역할", example = "MEMBER")
    Role role,
    @Schema(description = "프로필 이미지 URL", example = "default-profile.png")
    String profileImageUrl
) {

  public static SigninResponse from(Member member) {
    return new SigninResponse(
        member.getId(),
        member.getName(),
        member.getGym().getId(),
        member.getRole(),
        member.getProfileImageUri()
    );
  }
}
