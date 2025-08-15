package com.workout.trainer.domain;

import com.workout.trainer.dto.ProfileCreateDto.WorkexperiencesDto;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.util.Date;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Workexperiences {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_id", nullable = false)
  private Trainer trainer;

  private String workName;
  private String workPlace;
  private String workPosition;
  private LocalDate workStart;
  private LocalDate  workEnd;

  @Builder
  private Workexperiences(Trainer trainer, String workName, String workPlace, String workPosition, LocalDate  workStart, LocalDate  workEnd) {
    this.trainer = trainer;
    this.workName = workName;
    this.workPlace = workPlace;
    this.workPosition = workPosition;
    this.workStart = workStart;
    this.workEnd = workEnd;
  }

  public static Workexperiences of(WorkexperiencesDto workDto, Trainer trainer) {
    return Workexperiences.builder()
        .trainer(trainer)
        .workName(workDto.workName())
        .workPlace(workDto.workPlace())
        .workPosition(workDto.workPosition())
        .workStart(workDto.workStartDate())
        .workEnd(workDto.workEndDate())
        .build();
  }
}
