package com.workout.trainer.domain;

import com.workout.trainer.dto.ProfileCreateDto.AwardDto;
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
public class Award {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_id", nullable = false)
  private Trainer trainer;

  private String awardName;
  private String awardDate;
  private String awardPlace;

  @Builder
  private Award(Trainer trainer, String awardName, String awardDate, String awardPlace) {
    this.trainer = trainer;
    this.awardName = awardName;
    this.awardDate = awardDate;
    this.awardPlace = awardPlace;
  }

  public static Award of(AwardDto dto, Trainer trainer) {
    return Award.builder()
        .trainer(trainer)
        .awardName(dto.awardName())
        .awardDate(dto.awardDate())
        .awardPlace(dto.awardPlace())
        .build();
  }
}
