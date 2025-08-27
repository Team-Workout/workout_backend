package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PTErrorCode implements ErrorCode {
  NOT_ALLOWED_ACCESS(HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
  NOT_FOUND_PT_OFFERING(HttpStatus.NOT_FOUND, "pt정보를 찾을 수 없습니다."),
  NOT_FOUND_PT_APPLICATION(HttpStatus.NOT_FOUND, "PT 신청 정보를 찾을 수 없습니다."),
  NOT_FOUND_PT_APPOINTMENT(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다."),
  NOT_FOUND_PT_CONTRACT(HttpStatus.NOT_FOUND,"계약 정보를 찾을 수 없습니다"),
  NOT_FOUND_PT_SESSION(HttpStatus.NOT_FOUND, "PT세션을 찾을 수 없습니다."),
  INVALID_PARAMETER(HttpStatus.BAD_REQUEST, "요청 파라미터가 유효하지 않습니다."),
  NO_REMAIN_SESSION(HttpStatus.NOT_FOUND, "남은 PT 세션이 없습니다."),
  ALREADY_PRESENT_APPOINTMENT(HttpStatus.BAD_REQUEST, "해당 시간에 이미 다른 예약이 존재합니다."),
  INVALID_STATUS_REQUEST(HttpStatus.CONFLICT, "잘못된 상태의 요청입니다.");

  private final HttpStatus httpStatus;
  private final String message;
}