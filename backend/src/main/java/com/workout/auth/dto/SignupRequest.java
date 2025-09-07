package com.workout.auth.dto;

import com.workout.gym.domain.Gym;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Member;
import com.workout.member.domain.Role;
import com.workout.trainer.domain.Trainer;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "회원가입 요청 DTO")
public record SignupRequest(
    @Schema(description = "소속 헬스장 ID", nullable = false, example = "1") // [수정]
    @NotNull(message = "헬스장 ID는 필수입니다.")
    Long gymId,

    @Schema(description = "이메일 (로그인 ID)", nullable = false, example = "newbie@example.com")
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @Schema(description = "비밀번호 (영문/숫자 포함 8자 이상)", nullable = false, example = "newpass123")
    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "비밀번호는 영문과 숫자를 포함하여 8자 이상이어야 합니다.")
    String password,

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

  public Member toMemberEntity(Gym gym, String password) {
    return Member.builder()
        .gym(gym)
        .email(this.email)
        .password(password)
        .name(this.name)
        .gender(this.gender)
        .role(this.role)
        .accountStatus(AccountStatus.ACTIVE)
        .build();
  }

  public Trainer toTrainerEntity(Gym gym, String password) {
    return Trainer.builder()
        .gym(gym)
        .email(this.email)
        .password(password)
        .name(this.name)
        .gender(this.gender)
        .role(Role.TRAINER)
        .accountStatus(AccountStatus.ACTIVE)
        .build();
  }
}