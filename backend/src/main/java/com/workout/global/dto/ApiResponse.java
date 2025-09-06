package com.workout.global.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;
import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL) // null인 필드는 JSON으로 변환하지 않음
public record ApiResponse<T>(
    T data,
    PageInfo pageInfo // 페이징 정보 (페이징이 없는 경우 null)
) {
  /**
   * 페이징이 없는 단일/리스트 데이터 응답을 위한 정적 팩토리 메소드
   */
  public static <T> ApiResponse<T> of(T data) {
    return new ApiResponse<>(data, null);
  }

  /**
   * 페이징된 데이터 응답을 위한 정적 팩토리 메소드
   */
  public static <T> ApiResponse<List<T>> of(Page<T> page) {
    return new ApiResponse<>(page.getContent(), PageInfo.from(page));
  }

  /**
   * Creates an empty ApiResponse with no data and no pagination information.
   *
   * @return an ApiResponse instance with both data and pageInfo set to null
   */
  public static ApiResponse<Void> empty() {
    return new ApiResponse<>(null, null);
  }
}