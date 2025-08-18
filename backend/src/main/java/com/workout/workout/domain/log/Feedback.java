package com.workout.workout.domain.log;

import com.workout.global.BaseEntity;
import com.workout.member.domain.Member;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import java.util.stream.Stream;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
public class Feedback extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id")
  private Member author;

  @Lob
  @Column(columnDefinition = "TEXT", nullable = false)
  private String content;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_log_id")
  private WorkoutLog workoutLog;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_set_id")
  private WorkoutSet workoutSet;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_exercise_id")
  private WorkoutExercise workoutExercise;

  @Builder
  public Feedback(Member author, String content, WorkoutLog workoutLog,
      WorkoutExercise workoutExercise, WorkoutSet workoutSet) {
    // 피드백은 반드시 하나의 대상(Log, Exercise, Set)에만 속해야 함을 검증
    long count = Stream.of(workoutLog, workoutExercise, workoutSet)
        .filter(java.util.Objects::nonNull)
        .count();
    if (count != 1) {
      throw new IllegalArgumentException("피드백은 운동 일지, 운동, 또는 운동 세트 중 하나에만 정확히 속해야 합니다.");
    }

    this.author = author;
    this.content = content;
    this.workoutLog = workoutLog;
    this.workoutExercise = workoutExercise;
    this.workoutSet = workoutSet;
  }

  // 연관관계 편의 메소드
  protected void setWorkoutLog(WorkoutLog workoutLog) {
    this.workoutLog = workoutLog;
  }

  protected void setWorkoutExercise(WorkoutExercise workoutExercise) {
    this.workoutExercise = workoutExercise;
  }

  protected void setWorkoutSet(WorkoutSet workoutSet) {
    this.workoutSet = workoutSet;
  }
}