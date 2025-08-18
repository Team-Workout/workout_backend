package com.workout.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// 클래스를 record로 선언하면 생성자가 자동으로 생성됩니다.
public record SigninRequest(
    @NotBlank(message = "이메일은 필수 입력 값입니다.")
    @Email(message = "유효한 이메일 형식이 아닙니다.")
    String email,

    @NotBlank(message = "비밀번호는 필수 입력 값입니다.")
    String password
) {

}