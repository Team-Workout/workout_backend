package com.workout.trainer.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileCreateDto {

  private String introduction;
  private List<AwardDto> awards;
  private List<CertificationDto> certifications;
  private List<EducationDto> educations;
  private List<WorkexperiencesDto> workExperiences;
  
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class AwardDto {
    private String awardName;
    private String awardDate;
    private String awardPlace;
  }
  
  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class CertificationDto {
    private String certificationName;
    private String issuingOrganization;
    private String acquisitionDate;

  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class EducationDto {
    private String schoolName;
    private String educationName;
    private String degree;
    private String startDate;
    private String endDate;
  }

  @Getter
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WorkexperiencesDto {
    private String workName;
    private String workDate;
    private String workPlace;
    private String workPosition;
  }

}
