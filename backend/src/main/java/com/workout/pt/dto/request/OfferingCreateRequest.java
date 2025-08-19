package com.workout.pt.dto.request;

public record OfferingCreateRequest(
    String name,
    Long price,
    Long totalSessions,
    Long trainerId
) {

}
