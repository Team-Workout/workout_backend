package com.workout.pt.dto.request;

public record OfferingCreateRequest(
    String title,
    String description,
    Long price,
    Long totalSessions,
    Long trainerId
) {

}
