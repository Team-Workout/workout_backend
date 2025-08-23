package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum GymErrorCode implements ErrorCode {
  GYM_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않은 헬스장입니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
