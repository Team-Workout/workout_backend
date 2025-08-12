package com.workout.workout.domain.log;


import com.workout.global.AuditableEntity;
import com.workout.user.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Feedback extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "author_id")
  private User author; // 작성자

  @Lob // 긴 텍스트를 위해 @Lob 어노테이션 사용
  @Column(columnDefinition = "TEXT", nullable = false)
  private String content; // 메모 내용

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_log_id")
  private WorkoutLog workoutLog; // 운동 일지에 대한 참조

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_set_id")
  private WorkoutSet workoutSet; // 운동 세트에 대한 참조

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_exercise_id")
  private WorkoutExercise workoutExercise;

  @Builder
  public Feedback(User author, String content, WorkoutLog workoutLog, WorkoutExercise workoutExercise, WorkoutSet workoutSet) {
    // 피드백은 반드시 하나의 대상(Log, Exercise, Set)에만 속해야 함을 검증
    long count = java.util.stream.Stream.of(workoutLog, workoutExercise, workoutSet)
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