package com.workout.global.exception.errorcode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum FileErrorCode implements ErrorCode {
  NULL_IMAGE(HttpStatus.NOT_FOUND, "이미지가 없습니다"),
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
  INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "파일 타입이 유효하지 않습니다."),
  INVALID_FILE_NAME(HttpStatus.BAD_REQUEST, "파일 이름이 유효하지 않습니다."),
  INVALID_FILE_SIZE(HttpStatus.BAD_REQUEST, "파일 사이즈가 초과되었습니다."),
  NOT_AUTHORITY(HttpStatus.BAD_REQUEST, "권한이 없습니다."),
  FILE_STORAGE_FAILED(HttpStatus.CONFLICT, " 파일 저장에 실패했습니다"),
  FILE_DELETE_FAILED(HttpStatus.CONFLICT, "파일 삭제가 실패했습니다."),
  FAIL_HASH(HttpStatus.CONFLICT, "hash에 실패했습니다.");

  private final HttpStatus httpStatus;
  private final String message;
}
