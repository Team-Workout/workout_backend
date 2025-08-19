package com.workout.pt.dto;

import com.workout.pt.domain.PTApplication;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public record PendingApplicationResponse(
    List<Application> applications // '신청 목록'을 필드로 가집니다.
) {

  public static PendingApplicationResponse from(List<PTApplication> applicationList) {
    // PTApplication 엔티티 목록을 ApplicationItem DTO 목록으로 변환합니다.
    List<Application> applicationItems = applicationList.stream()
        .map(Application::from) // 각 Application을 ApplicationItem으로 매핑
        .collect(Collectors.toList());

    // 변환된 목록을 포함하는 최종 응답 객체를 생성하여 반환합니다.
    return new PendingApplicationResponse(applicationItems);
  }

  public record Application(
      Long applicationId,
      String memberName,
      Instant appliedAt,
      Long totalSessions
  ) {
    public static Application from(PTApplication application) {
      return new Application(
          application.getId(),
          application.getMember().getName(),
          application.getCreatedAt(),
          application.getTotalSessions()
      );
    }
  }
}