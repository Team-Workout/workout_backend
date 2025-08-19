package com.workout.pt.dto.request;

import jakarta.validation.constraints.NotBlank;

public record PtApplicationRequest(
    @NotBlank(message = "offeringid는 비워둘 수 없습니다.")
    Long offeringId
) {

}