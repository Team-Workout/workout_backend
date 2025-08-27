package com.workout.pt.dto.response;

import com.workout.pt.domain.contract.PTAppointment;
import java.time.LocalDateTime;
import java.util.List;

public record AppointmentResponse(
    Long id,
    Long contractId,
    String trainerName,
    String memberName,
    LocalDateTime startTime,
    LocalDateTime endTime
) {
  public static AppointmentResponse from(PTAppointment appointments) {
    return new AppointmentResponse(
        appointments.getId(),
        appointments.getContract().getId(),
        appointments.getContract().getTrainer().getName(),
        appointments.getContract().getMember().getName(),
        appointments.getStartTime(),
        appointments.getEndTime()
    );
  }
}
