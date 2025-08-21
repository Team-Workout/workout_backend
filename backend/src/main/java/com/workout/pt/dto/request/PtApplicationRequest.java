package com.workout.pt.dto.request;

import jakarta.validation.constraints.NotNull;

public record PtApplicationRequest(
    @NotNull
    Long offeringId
) {

}