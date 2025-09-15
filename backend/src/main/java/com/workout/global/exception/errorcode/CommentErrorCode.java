package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CommentErrorCode implements ErrorCode {
  NOT_AUTHORITY(HttpStatus.BAD_REQUEST, "권한이 없습니다."),
  INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "잘못된 매개변수 입니다."),
  NOT_FOUND(HttpStatus.NOT_FOUND, "comment를 찾을 수 없습니다.");
  private final HttpStatus httpStatus;
  private final String message;
}
