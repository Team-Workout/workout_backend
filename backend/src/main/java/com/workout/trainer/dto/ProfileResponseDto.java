package com.workout.trainer.dto;

import com.workout.trainer.domain.*; // Award, Certification 등 모든 도메인 import

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
      List<Workexperiences> workExperiences,
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
        specialties.stream().map(Enum::name).collect(Collectors.toSet())
    );
  }

  public record AwardDto(String awardName, String awardDate, String awardPlace) {
    public static AwardDto from(Award award) {
      return new AwardDto(award.getAwardName(), award.getAwardDate(), award.getAwardPlace());
    }
  }

  public record CertificationDto(String certificationName, String issuingOrganization, String acquisitionDate) {
    public static CertificationDto from(Certification certification) {
      return new CertificationDto(certification.getCertificationName(), certification.getIssuingOrganization(), certification.getAcquisitionDate().toString());
    }
  }

  public record EducationDto(String schoolName, String educationName, String degree, String startDate, String endDate) {
    public static EducationDto from(Education education) {
      return new EducationDto(education.getSchoolName(), education.getEducationName(), education.getDegree(), education.getStartDate(), education.getEndDate());
    }
  }

  public record WorkExperienceDto(String workName, String workPlace, String workPosition, String workStart, String workEnd) {
    public static WorkExperienceDto from(Workexperiences workExperience) {
      return new WorkExperienceDto(workExperience.getWorkName(), workExperience.getWorkPlace(), workExperience.getWorkPosition(), workExperience.getWorkStart(), workExperience.getWorkEnd());
    }
  }
}