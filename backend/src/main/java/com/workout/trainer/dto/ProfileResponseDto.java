package com.workout.trainer.dto;

import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Certification;
import com.workout.trainer.domain.Education;
import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.domain.Workexperience;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record ProfileResponseDto(
    Long trainerId,
    String name,
    String email,
    String introduction,
    List<AwardDto> awards,
    List<CertificationDto> certifications,
    List<EducationDto> educations,
    List<WorkExperienceDto> workExperiences,
    Set<String> specialties
) {

  public static ProfileResponseDto from(
      Trainer trainer,
      List<Award> awards,
      List<Certification> certifications,
      List<Education> educations,
      List<Workexperience> workExperiences,
      Set<Specialty> specialties
  ) {
    return new ProfileResponseDto(
        trainer.getId(),
        trainer.getName(),
        trainer.getEmail(),
        trainer.getIntroduction(),
        awards.stream().map(AwardDto::from).collect(Collectors.toList()),
        certifications.stream().map(CertificationDto::from).collect(Collectors.toList()),
        educations.stream().map(EducationDto::from).collect(Collectors.toList()),
        workExperiences.stream().map(WorkExperienceDto::from).collect(Collectors.toList()),
        specialties.stream()
            .map(Specialty::getName)
            .collect(Collectors.toSet())
    );
  }

  public record AwardDto(
      Long id,
      String awardName,
      LocalDate awardDate,
      String awardPlace
  ) {

    public static AwardDto from(Award award) {
      return new AwardDto(award.getId(), award.getAwardName(), award.getAwardDate(),
          award.getAwardPlace());
    }
  }

  public record CertificationDto(
      Long id,
      String certificationName,
      String issuingOrganization,
      LocalDate acquisitionDate
  ) {

    public static CertificationDto from(Certification certification) {
      return new CertificationDto(certification.getId(), certification.getCertificationName(),
          certification.getIssuingOrganization(), certification.getAcquisitionDate());
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

    public static EducationDto from(Education education) {
      return new EducationDto(education.getId(), education.getSchoolName(),
          education.getEducationName(),
          education.getDegree(), education.getStartDate(), education.getEndDate());
    }
  }

  public record WorkExperienceDto(
      Long id,
      String workName,
      String workPlace,
      String workPosition,
      LocalDate workStart,
      LocalDate workEnd
  ) {

    public static WorkExperienceDto from(Workexperience workExperience) {
      return new WorkExperienceDto(workExperience.getId(), workExperience.getWorkName(),
          workExperience.getWorkPlace(),
          workExperience.getWorkPosition(), workExperience.getWorkStart(),
          workExperience.getWorkEnd());
    }
  }
}