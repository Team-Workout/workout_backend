package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum BodyErrorCode implements ErrorCode {
  BODY_COMPOSITION_NOT_FOUND(HttpStatus.NOT_FOUND, "체성분이 존재하지 않습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
