package com.workout.auth.dto;

import com.workout.gym.domain.Gym;
import com.workout.trainer.domain.Trainer;
import com.workout.user.domain.AccountStatus;
import com.workout.user.domain.Gender;
import com.workout.user.domain.Member;
import com.workout.user.domain.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


public record SignupRequest(
    @NotNull(message = "헬스장 ID는 필수입니다.")
    Long gymId,

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수입니다.")
    @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d).{8,}$", message = "비밀번호는 영문과 숫자를 포함하여 8자 이상이어야 합니다.")
    String password,

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, message = "이름은 2자 이상이어야 합니다.")
    String name,

    @NotNull(message = "성별은 필수입니다.")
    Gender gender,

    @NotNull(message = "사용자 역할은 필수입니다.")
    Role role
) {
    public Member toMemberEntity(Gym gym, String encodedPassword) {
        return Member.builder()
            .gym(gym)
            .email(this.email)
            .password(encodedPassword)
            .name(this.name)
            .gender(this.gender)
            .role(this.role) // DTO에 따라 USER 또는 ADMIN이 될 것임
            .accountStatus(AccountStatus.ACTIVE)
            .build();
    }

    public Trainer toTrainerEntity(Gym gym, String encodedPassword) {
        return Trainer.builder()
            .gym(gym)
            .email(this.email)
            .password(encodedPassword)
            .name(this.name)
            .gender(this.gender)
            .role(Role.TRAINER) // 역할은 TRAINER로 고정
            .accountStatus(AccountStatus.ACTIVE)
            .build();
    }
}