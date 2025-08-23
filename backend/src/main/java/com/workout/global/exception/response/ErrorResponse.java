package com.workout.global.exception.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.FieldError;

@Getter
@Builder
@RequiredArgsConstructor
public class ErrorResponse {

  private final String code;
  private final String message;

  // @Valid 에러 처리를 위한 내부 정적 클래스
  @JsonInclude(JsonInclude.Include.NON_EMPTY) // errors 리스트가 비어있으면 응답에서 제외
  private final List<ValidationError> errors;

  @Getter
  @Builder
  @RequiredArgsConstructor
  public static class ValidationError {

    private final String field;
    private final String message;

    public static ValidationError of(final FieldError fieldError) {
      return ValidationError.builder()
          .field(fieldError.getField())
          .message(fieldError.getDefaultMessage())
          .build();
    }
  }
}