package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WorkoutErrorCode implements ErrorCode {
  NOT_ALLOWED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
  NOT_FOUND_WORKOUT_LOG(HttpStatus.NOT_FOUND,"운동일지를 찾을 수 없습니다"),
  NOT_FOUND_ROUTINE(HttpStatus.NOT_FOUND, "루틴을 찾을 수 없습니다"),
  NOT_FOUND_EXERCISE(HttpStatus.NOT_FOUND, "운동을 찾울 수 없습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}