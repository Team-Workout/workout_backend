package com.workout.member.dto;

import jakarta.validation.constraints.NotNull;

public record MemberSettingsDto(

    @NotNull // PUT 요청 시 모든 값은 필수입니다.
    Boolean isOpenWorkoutRecord,

    @NotNull
    Boolean isOpenBodyImg,

    @NotNull
    Boolean isOpenBodyComposition
) {
}