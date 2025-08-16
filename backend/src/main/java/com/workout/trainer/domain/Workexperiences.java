package com.workout.trainer.domain;

import com.workout.trainer.dto.ProfileCreateDto.WorkexperiencesDto;
import com.workout.workout.domain.log.WorkoutLog;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Date;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "work_experience")
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) {
      return false;
    }

    Workexperiences that = (Workexperiences) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}
