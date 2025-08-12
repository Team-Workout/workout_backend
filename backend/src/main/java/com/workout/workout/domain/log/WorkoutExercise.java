package com.workout.workout.domain.log;

import com.workout.workout.domain.exercise.Exercise;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutExercise {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // [수정] 연관관계 편의 메소드에서 workoutLog를 설정하므로 nullable, updatable = false 추가
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "workout_log_id", nullable = false, updatable = false)
  private WorkoutLog workoutLog;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "exercise_id")
  private Exercise exercise;

  @Column(name = "log_order", nullable = false)
  private int order;

  @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("order ASC")
  private List<WorkoutSet> workoutSets = new ArrayList<>();

  // [수정] mappedBy 속성 오류 수정 ("workoutLog" -> "workoutExercise")
  @OneToMany(mappedBy = "workoutExercise", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Feedback> feedbacks = new HashSet<>();

  // [수정] 생성자에서 workoutLog 주입 제거 (연관관계 편의 메소드로 위임)
  @Builder
  public WorkoutExercise(Exercise exercise, int order) {
    this.exercise = exercise;
    this.order = order;
  }

  //== 연관관계 편의 메소드 ==//
  protected void setWorkoutLog(WorkoutLog workoutLog) {
    this.workoutLog = workoutLog;
  }

  public void addWorkoutSet(WorkoutSet workoutSet) {
    this.workoutSets.add(workoutSet);
    workoutSet.setWorkoutExercise(this);
  }

  public void addFeedback(Feedback feedback) {
    this.feedbacks.add(feedback);
    feedback.setWorkoutExercise(this);
  }
}