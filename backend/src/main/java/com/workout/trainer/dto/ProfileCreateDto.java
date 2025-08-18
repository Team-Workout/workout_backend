package com.workout.trainer.dto;

import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Certification;
import com.workout.trainer.domain.Education;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.domain.Workexperience;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public record ProfileCreateDto(
    String introduction,
    List<AwardDto> awards,
    List<CertificationDto> certifications,
    List<EducationDto> educations,
    List<WorkExperienceDto> workExperiences,
    Set<String> specialties // 누락된 필드 추가
) {

  public record AwardDto(
      Long id,
      String awardName,
      LocalDate awardDate,
      String awardPlace
  ) {

    public Award toEntity(Trainer trainer) {
      return Award.builder().trainer(trainer).awardName(awardName).awardDate(awardDate)
          .awardPlace(awardPlace).build();
    }
  }

  public record CertificationDto(
      Long id,
      String certificationName,
      String issuingOrganization,
      LocalDate acquisitionDate
  ) {

    public Certification toEntity(Trainer trainer) {
      return Certification.builder().trainer(trainer).certificationName(certificationName)
          .issuingOrganization(issuingOrganization).acquisitionDate(acquisitionDate).build();
    }
  }

  public record EducationDto(
      Long id,
      String schoolName,
      String educationName,
      String degree,
      LocalDate startDate,
      LocalDate endDate
  ) {

    public Education toEntity(Trainer trainer) {
      return Education.builder().trainer(trainer).schoolName(schoolName)
          .educationName(educationName).degree(degree).startDate(startDate).endDate(endDate)
          .build();
    }
  }

  public record WorkExperienceDto(
      Long id,
      String workName,
      LocalDate workStartDate, // DTO 필드명
      LocalDate workEndDate,   // DTO 필드명
      String workPlace,
      String workPosition
  ) {

    public Workexperience toEntity(Trainer trainer) {
      return Workexperience.builder()
          .trainer(trainer)
          .workName(workName)
          .workPlace(workPlace)
          .workPosition(workPosition)
          .workStart(workStartDate) // workStart -> workStartDate 로 수정
          .workEnd(workEndDate)     // workEnd -> workEndDate 로 수정
          .build();
    }
  }
}