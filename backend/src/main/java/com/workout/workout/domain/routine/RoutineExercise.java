package com.workout.workout.domain.routine;

import com.workout.workout.domain.exercise.Exercise;
import jakarta.persistence.Column;
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

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RoutineExercise {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "routine_id", nullable = false, updatable = false)
  private Routine routine;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exercise_id")
  private Exercise exercise;

  @Column(name = "routine_order", nullable = false)
  private Integer order;


  @Builder
  public RoutineExercise(Exercise exercise, Integer order, Routine routine) {
    this.exercise = exercise;
    this.order = order;
    this.routine = routine;
  }

  protected void setRoutine(Routine routine) {
    this.routine = routine;
  }
}
