package com.workout.trainer.dto;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public record ProfileCreateDto(
    String introduction,
    List<AwardDto> awards,
    List<CertificationDto> certifications,
    List<EducationDto> educations,
    List<WorkexperiencesDto> workExperiences
) {

  public record AwardDto(
      String awardName,
      LocalDate  awardDate,
      String awardPlace
  ) {}

  public record CertificationDto(
      String certificationName,
      String issuingOrganization,
      LocalDate  acquisitionDate
  ) {}

  public record EducationDto(
      String schoolName,
      String educationName,
      String degree,
      LocalDate  startDate,
      LocalDate  endDate
  ) {}

  public record WorkexperiencesDto(
      String workName,
      LocalDate workStartDate,
      LocalDate  workEndDate,
      String workPlace,
      String workPosition
  ) {}
}