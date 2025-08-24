package com.workout.global.dto;

import org.springframework.data.domain.Page;

/**
 * 페이징 정보를 담는 DTO
 *
 * @param page          현재 페이지 번호 (0부터 시작)
 * @param size          페이지 당 데이터 수
 * @param totalElements 전체 데이터 수
 * @param totalPages    전체 페이지 수
 * @param last          마지막 페이지 여부
 */
public record PageInfo(
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean last
) {

  /**
   * Spring Data의 Page 객체를 사용하여 PageInfo DTO를 생성하는 정적 팩토리 메소드
   */
  public static PageInfo from(Page<?> page) {
    return new PageInfo(
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast()
    );
  }
}