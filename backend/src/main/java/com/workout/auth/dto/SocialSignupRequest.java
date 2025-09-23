package com.workout.auth.dto;

import com.workout.member.domain.Gender;
import com.workout.member.domain.Role;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SocialSignupRequest(
    @Schema(description = "소속 헬스장 ID", nullable = false, example = "1") // [수정]
    @NotNull(message = "헬스장 ID는 필수입니다.")
    Long gymId,

    @Schema(description = "사용자 실명", nullable = false, example = "김철수")
    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, message = "이름은 2자 이상이어야 합니다.")
    String name,

    @Schema(description = "성별", nullable = false, example = "MALE")
    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @Schema(description = "요청하는 역할 (MEMBER 또는 TRAINER)", nullable = false, example = "MEMBER")
    @NotNull(message = "사용자 역할은 필수입니다.")
    Role role
) {

}
