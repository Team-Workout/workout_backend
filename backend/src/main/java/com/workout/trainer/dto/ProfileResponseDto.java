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
    String profileImageUrl,
    Set<AwardDto> awards,
    Set<CertificationDto> certifications,
    Set<EducationDto> educations,
    Set<WorkExperienceDto> workExperiences,
    Set<String> specialties
) {
/*

  public static ProfileResponseDto from(
      Trainer trainer,
      String profileImageUrl,
      Set<Award> awards,
      Set<Certification> certifications,
      Set<Education> educations,
      Set<Workexperience> workExperiences,
      Set<Specialty> specialties
  ) {
    return new ProfileResponseDto(
        trainer.getId(),
        trainer.getName(),
        trainer.getEmail(),
        trainer.getIntroduction(),
        profileImageUrl,
        awards.stream().map(AwardDto::from).collect(Collectors.toSet()),
        certifications.stream().map(CertificationDto::from).collect(Collectors.toSet()),
        educations.stream().map(EducationDto::from).collect(Collectors.toSet()),
        workExperiences.stream().map(WorkExperienceDto::from).collect(Collectors.toSet()),
        specialties.stream()
            .map(Specialty::getName)
            .collect(Collectors.toSet())
    );
  }
*/

  public static ProfileResponseDto fromEntity(Trainer trainer) {
    if (trainer == null) {
      return null;
    }

    Set<AwardDto> awardDtos = trainer.getAwards().stream()
        .map(AwardDto::fromEntity)
        .collect(Collectors.toSet());

    Set<CertificationDto> certificationDtos = trainer.getCertifications().stream()
        .map(CertificationDto::fromEntity)
        .collect(Collectors.toSet());

    Set<EducationDto> educationDtos = trainer.getEducations().stream()
        .map(EducationDto::fromEntity)
        .collect(Collectors.toSet());

    Set<WorkExperienceDto> workExperienceDtos = trainer.getWorkExperiences().stream()
        .map(WorkExperienceDto::fromEntity)
        .collect(Collectors.toSet());

    Set<String> specialtyNames = trainer.getTrainerSpecialties().stream()
        .map(trainerSpecialty -> trainerSpecialty.getSpecialty().getName())
        .collect(Collectors.toSet());

    return new ProfileResponseDto(
        trainer.getId(),
        trainer.getName(),
        trainer.getEmail(),
        trainer.getIntroduction(),
        trainer.getProfileImageUri(),
        awardDtos,
        certificationDtos,
        educationDtos,
        workExperienceDtos,
        specialtyNames
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

      public static AwardDto fromEntity(Award award) {
        return new AwardDto(award.getId(), award.getAwardName(), award.getAwardDate(), award.getAwardPlace());
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

    public static CertificationDto fromEntity(Certification certification) {
      return new CertificationDto(certification.getId(), certification.getCertificationName(), certification.getIssuingOrganization(), certification.getAcquisitionDate());
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

    public static EducationDto fromEntity(Education education) {
      return new EducationDto(education.getId(), education.getSchoolName(), education.getEducationName(), education.getDegree(), education.getStartDate(), education.getEndDate());
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

    public static WorkExperienceDto fromEntity(Workexperience workExperience) {
      return new WorkExperienceDto(workExperience.getId(), workExperience.getWorkName(), workExperience.getWorkPlace(), workExperience.getWorkPosition(), workExperience.getWorkStart(), workExperience.getWorkEnd());
    }
  }
}