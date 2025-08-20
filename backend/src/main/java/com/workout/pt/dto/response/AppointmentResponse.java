package com.workout.pt.dto.response;

import com.workout.pt.domain.contract.PTAppointment;
import java.util.List;

public record AppointmentResponse(
    List<PTAppointment> appointments
) {
  public static AppointmentResponse from(List<PTAppointment> appointments) {
    return new AppointmentResponse(appointments);
  }
}
