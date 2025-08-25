package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ProfileErrorCode implements ErrorCode {
  NOT_FOUND_PROFILE(HttpStatus.NOT_FOUND,"프로필을 찾을 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}