package com.workout.pt.dto.response;

import com.workout.pt.domain.contract.PTAppointment;
import java.time.LocalDateTime;
import java.util.List;

public record AppointmentResponse(
    Long id,
    Long contractId,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
  public static AppointmentResponse from(PTAppointment appointments) {
    return new AppointmentResponse(
        appointments.getId(),
        appointments.getContract().getId(),
        appointments.getStartTime(),
        appointments.getEndTime()
    );
  }
}
