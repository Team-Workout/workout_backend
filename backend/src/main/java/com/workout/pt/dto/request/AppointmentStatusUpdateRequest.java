package com.workout.pt.dto.request;

import com.workout.pt.domain.contract.PTAppointmentStatus;

public record AppointmentStatusUpdateRequest(
    PTAppointmentStatus status
) {

}
