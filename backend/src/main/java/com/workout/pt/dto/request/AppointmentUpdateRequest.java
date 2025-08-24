package com.workout.pt.dto.request;

import java.time.LocalDateTime;

public record AppointmentUpdateRequest(
    LocalDateTime newStartTime,
    LocalDateTime newEndTime
) {

}
