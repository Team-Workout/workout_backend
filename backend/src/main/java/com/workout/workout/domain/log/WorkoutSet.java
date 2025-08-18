package com.workout.workout.domain.log;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.math.BigDecimal;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_exercise_id")
  private WorkoutExercise workoutExercise;

  @Column(name = "set_order", nullable = false)
  private int order;

  @Column(nullable = false)
  @ColumnDefault("0.00")
  private BigDecimal weight;

  @Column(nullable = false)
  private int reps;

  @Builder
  public WorkoutSet(int order, BigDecimal weight, int reps, WorkoutExercise workoutExercise) {
    this.order = order;
    this.weight = weight;
    this.reps = reps;
    this.workoutExercise = workoutExercise;
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

    WorkoutSet that = (WorkoutSet) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}