package com.workout.trainer.dto;

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
      String awardDate,
      String awardPlace
  ) {}

  public record CertificationDto(
      String certificationName,
      String issuingOrganization,
      String acquisitionDate
  ) {}

  public record EducationDto(
      String schoolName,
      String educationName,
      String degree,
      String startDate,
      String endDate
  ) {}

  public record WorkexperiencesDto(
      String workName,
      String workStartDate,
      String workEndDate,
      String workPlace,
      String workPosition
  ) {}
}