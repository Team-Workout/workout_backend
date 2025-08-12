package com.workout.workout.dto.log;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class WorkoutLogResponse {

  private final Long workoutLogId;
  private final LocalDate workoutDate;
  private final Set<FeedbackResponse> feedbacks;
  private final List<WorkoutExerciseResponse> workoutExercises;

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

  @JsonCreator
  public WorkoutLogResponse(
      @JsonProperty("workoutLogId") Long workoutLogId,
      @JsonProperty("workoutDate") LocalDate workoutDate,
      @JsonProperty("feedbacks") Set<FeedbackResponse> feedbacks,
      @JsonProperty("workoutExercises") List<WorkoutExerciseResponse> workoutExercises) {
    this.workoutLogId = workoutLogId;
    this.workoutDate = workoutDate;
    this.feedbacks = feedbacks;
    this.workoutExercises = workoutExercises;
  }

  public static WorkoutLogResponse from(WorkoutLog workoutLog) {
    return new WorkoutLogResponse(workoutLog);
  }

  @Getter
  public static class WorkoutExerciseResponse {
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

    @JsonCreator
    public WorkoutExerciseResponse(
        @JsonProperty("workoutExerciseId") Long workoutExerciseId,
        @JsonProperty("exerciseName") String exerciseName,
        @JsonProperty("order") int order,
        @JsonProperty("workoutSets") List<WorkoutSetResponse> workoutSets) {
      this.workoutExerciseId = workoutExerciseId;
      this.exerciseName = exerciseName;
      this.order = order;
      this.workoutSets = workoutSets;
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

    @JsonCreator
    public WorkoutSetResponse(
        @JsonProperty("workoutSetId") Long workoutSetId,
        @JsonProperty("order") int order,
        @JsonProperty("weight") BigDecimal weight,
        @JsonProperty("reps") int reps,
        @JsonProperty("feedbacks") Set<FeedbackResponse> feedbacks) {
      this.workoutSetId = workoutSetId;
      this.order = order;
      this.weight = weight;
      this.reps = reps;
      this.feedbacks = feedbacks;
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

    @JsonCreator
    public FeedbackResponse(
        @JsonProperty("feedbackId") Long feedbackId,
        @JsonProperty("authorName") String authorName,
        @JsonProperty("content") String content) {
      this.feedbackId = feedbackId;
      this.authorName = authorName;
      this.content = content;
    }

    public static FeedbackResponse from(Feedback feedback) {
      return new FeedbackResponse(feedback);
    }
  }
}