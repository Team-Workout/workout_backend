package com.workout.global.exception.handler;

import com.workout.global.exception.response.ErrorResponse;
import com.workout.global.exception.response.ErrorResponse.ValidationError;
import com.workout.global.exception.errorcode.CommonErrorCode;
import com.workout.global.exception.errorcode.ErrorCode;
import com.workout.global.exception.RestApiException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  // 1. 직접 정의한 RestApiException 처리
  @ExceptionHandler(RestApiException.class)
  public ResponseEntity<Object> handleCustomException(RestApiException e) {
    ErrorCode errorCode = e.getErrorCode();
    return handleExceptionInternal(errorCode);
  }

  // 2. IllegalArgumentException 처리 (잘못된 인자 전달)
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<Object> handleIllegalArgument(IllegalArgumentException e) {
    log.warn("handleIllegalArgument", e);
    ErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
    return handleExceptionInternal(errorCode, e.getMessage());
  }

  // 3. @Valid 어노테이션을 통한 유효성 검사 실패 시 처리
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    log.warn("handleMethodArgumentNotValid", ex);
    ErrorCode errorCode = CommonErrorCode.INVALID_PARAMETER;
    return handleExceptionInternal(ex, errorCode);
  }

  // 4. 위에서 처리되지 못한 모든 예외 처리
  @ExceptionHandler({Exception.class})
  public ResponseEntity<Object> handleAllException(Exception ex) {
    log.warn("handleAllException", ex);
    ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
    return handleExceptionInternal(errorCode);
  }

  // ErrorResponse를 생성하여 ResponseEntity에 담아 반환하는 공통 메서드들
  private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(makeErrorResponse(errorCode));
  }

  private ErrorResponse makeErrorResponse(ErrorCode errorCode) {
    return ErrorResponse.builder()
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .build();
  }

  private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode, String message) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(makeErrorResponse(errorCode, message));
  }

  private ErrorResponse makeErrorResponse(ErrorCode errorCode, String message) {
    return ErrorResponse.builder()
        .code(errorCode.name())
        .message(message)
        .build();
  }

  // BindException(@Valid) 처리 메서드
  private ResponseEntity<Object> handleExceptionInternal(BindException e, ErrorCode errorCode) {
    return ResponseEntity.status(errorCode.getHttpStatus())
        .body(makeErrorResponse(e, errorCode));
  }

  private ErrorResponse makeErrorResponse(BindException e, ErrorCode errorCode) {
    List<ValidationError> validationErrorList = e.getBindingResult()
        .getFieldErrors()
        .stream()
        .map(ErrorResponse.ValidationError::of)
        .collect(Collectors.toList());

    return ErrorResponse.builder()
        .code(errorCode.name())
        .message(errorCode.getMessage())
        .errors(validationErrorList)
        .build();
  }
}