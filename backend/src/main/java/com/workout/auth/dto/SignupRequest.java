package com.workout.auth.dto;

import com.workout.gym.domain.Gym;
import com.workout.user.domain.AccountStatus;
import com.workout.user.domain.Gender;
import com.workout.user.domain.Role;
import com.workout.user.domain.User;
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
    public User toEntity(Gym gym, String encodedPassword) {
        return User.builder()
            .gym(gym) // 파라미터로 받은 gym 엔티티
            .email(this.email)
            .password(encodedPassword) // 파라미터로 받은 암호화된 비밀번호
            .name(this.name)
            .gender(this.gender)
            .role(this.role)
            .accountStatus(AccountStatus.ACTIVE) // 기본값 설정
            .build();
    }
}