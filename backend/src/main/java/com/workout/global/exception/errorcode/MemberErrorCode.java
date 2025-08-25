package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberErrorCode implements ErrorCode {
  INACTIVE_USER(HttpStatus.FORBIDDEN, "User is inactive"),
  AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "이메일 또는 비밀번호가 일치하지 않습니다."),
  MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "가입되지 않은 이메일입니다."),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, "존재하는 이메일입니다."),
  UNKNOWN_MEMBER_ROLE(HttpStatus.CONFLICT, "유효하지 않은 유저 role입니다");

  private final HttpStatus httpStatus;
  private final String message;
}