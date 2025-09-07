package com.workout.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "로그인 요청 DTO")
public record SigninRequest(
    @Schema(description = "사용자 이메일", nullable = false, example = "user@example.com") // [수정]
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email,

    @Schema(description = "사용자 비밀번호", nullable = false, example = "password123!") // [수정]
    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    String password
) {

}