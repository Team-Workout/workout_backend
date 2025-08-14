package com.workout.trainer.domain;

import com.workout.trainer.dto.ProfileCreateDto.EducationDto;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Education {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_id", nullable = false)
  private Trainer trainer;

  private String schoolName;

  private String educationName;
  private String degree;
  private String startDate;
  private String endDate;

  @Builder
  private Education(Trainer trainer, String schoolName, String educationName, String degree, String startDate, String endDate) {
    this.trainer = trainer;
    this.schoolName = schoolName;
    this.educationName = educationName;
    this.degree = degree;
    this.startDate = startDate;
    this.endDate = endDate;
  }

  public static Education of(EducationDto eduDto, Trainer trainer) {
    return Education.builder()
        .trainer(trainer)
        .schoolName(eduDto.schoolName())
        .educationName(eduDto.educationName())
        .degree(eduDto.degree())
        .startDate(eduDto.startDate())
        .endDate(eduDto.endDate())
        .build();
  }
}
