package com.workout.workout.dto.workOut;

import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import java.util.Set;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class WorkoutLogResponse {

  private final Long workoutLogId;
  private final LocalDate workoutDate;
  private final Set<FeedbackResponse> feedbacks;
  private final List<WorkoutExerciseResponse> workoutExercises; // [수정] workoutSets -> workoutExercises

  private WorkoutLogResponse(WorkoutLog workoutLog) {
    this.workoutLogId = workoutLog.getId();
    this.workoutDate = workoutLog.getWorkoutDate();
    this.feedbacks = workoutLog.getFeedbacks().stream()
        .map(FeedbackResponse::from)
        .collect(Collectors.toSet());
    this.workoutExercises = workoutLog.getWorkoutExercises().stream()
        .map(WorkoutExerciseResponse::from)
        .collect(Collectors.toList());
  }

  public static WorkoutLogResponse from(WorkoutLog workoutLog) {
    return new WorkoutLogResponse(workoutLog);
  }

  @Getter
  public static class WorkoutExerciseResponse { // [신규] 운동 그룹을 위한 응답 DTO
    private final Long workoutExerciseId;
    private final String exerciseName;
    private final int order;
    private final List<WorkoutSetResponse> workoutSets;

    private WorkoutExerciseResponse(WorkoutExercise workoutExercise) {
      this.workoutExerciseId = workoutExercise.getId();
      this.exerciseName = workoutExercise.getExercise().getName();
      this.order = workoutExercise.getOrder();
      this.workoutSets = workoutExercise.getWorkoutSets().stream()
          .map(WorkoutSetResponse::from)
          .collect(Collectors.toList());
    }

    public static WorkoutExerciseResponse from(WorkoutExercise workoutExercise) {
      return new WorkoutExerciseResponse(workoutExercise);
    }
  }

  @Getter
  public static class WorkoutSetResponse {
    private final Long workoutSetId;
    private final int order;
    private final BigDecimal weight;
    private final int reps;
    private final Set<FeedbackResponse> feedbacks;

    private WorkoutSetResponse(WorkoutSet workoutSet) {
      this.workoutSetId = workoutSet.getId();
      this.order = workoutSet.getOrder();
      this.weight = workoutSet.getWeight();
      this.reps = workoutSet.getReps();
      this.feedbacks = workoutSet.getFeedbacks().stream()
          .map(FeedbackResponse::from)
          .collect(Collectors.toSet());
    }

    public static WorkoutSetResponse from(WorkoutSet workoutSet) {
      return new WorkoutSetResponse(workoutSet);
    }
  }

  @Getter
  public static class FeedbackResponse {
    private final Long feedbackId;
    private final String authorName;
    private final String content;

    private FeedbackResponse(Feedback feedback) {
      this.feedbackId = feedback.getId();
      this.authorName = feedback.getAuthor().getName();
      this.content = feedback.getContent();
    }

    public static FeedbackResponse from(Feedback feedback) {
      return new FeedbackResponse(feedback);
    }
  }
}