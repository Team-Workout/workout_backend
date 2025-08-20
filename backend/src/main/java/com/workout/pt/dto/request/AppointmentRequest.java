package com.workout.pt.dto.request;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AppointmentRequest(
    LocalDateTime startTime,
    LocalDateTime endTime,
    Long trainerId,
    Long memberId,
    Long gymId,
    Long contractId
) {

  public static AppointmentRequest from(LocalDateTime startTime,LocalDateTime endTime, Long trainerId, Long memberId, Long gymId,
      Long contractId) {
    return new AppointmentRequest(startTime, endTime, trainerId, memberId, gymId, contractId);
  }
}
