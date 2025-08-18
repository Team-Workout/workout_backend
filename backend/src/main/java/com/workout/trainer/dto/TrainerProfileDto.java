package com.workout.trainer.dto;

import java.time.LocalDate;

// JPQL JOIN 결과를 직접 매핑하기 위한 중간 DTO
public record TrainerProfileDto(
    // Trainer 정보
    Long trainerId,
    String name,
    String email,
    String introduction,

    // Award 정보 (LEFT JOIN이므로 Null일 수 있음)
    Long awardId,
    String awardName,
    LocalDate awardDate,
    String awardPlace,

    // Certification 정보 (LEFT JOIN이므로 Null일 수 있음)
    Long certificationId,
    String certificationName,
    String issuingOrganization,
    LocalDate acquisitionDate,

    // Education 정보 (LEFT JOIN이므로 Null일 수 있음)
    Long educationId,
    String schoolName,
    String educationName,
    String degree,
    LocalDate startDate,
    LocalDate endDate,

    // WorkExperience 정보 (LEFT JOIN이므로 Null일 수 있음)
    Long workExperienceId,
    String workName,
    String workPlace,
    String workPosition,
    LocalDate workStart,
    LocalDate workEnd
) {

}