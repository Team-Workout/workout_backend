package com.workout.workout.dto.log;

import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public record WorkoutLogResponse(
    Long workoutLogId,
    LocalDate workoutDate,
    Set<FeedbackResponse> feedbacks,
    List<WorkoutExerciseResponse> workoutExercises
) {

  public static WorkoutLogResponse from(WorkoutLog workoutLog) {
    // 엔티티의 컬렉션을 각각의 DTO 컬렉션으로 변환
    Set<FeedbackResponse> feedbackResponses = workoutLog.getFeedbacks().stream()
        .map(FeedbackResponse::from)
        .collect(Collectors.toSet());

    List<WorkoutExerciseResponse> workoutExerciseResponses = workoutLog.getWorkoutExercises().stream()
        .map(WorkoutExerciseResponse::from)
        .collect(Collectors.toList());

    // 변환된 데이터로 record 생성자를 호출하여 반환
    return new WorkoutLogResponse(
        workoutLog.getId(),
        workoutLog.getWorkoutDate(),
        feedbackResponses,
        workoutExerciseResponses
    );
  }

  public record WorkoutExerciseResponse(
      Long workoutExerciseId,
      String exerciseName,
      int order,
      List<WorkoutSetResponse> workoutSets
  ) {
    public static WorkoutExerciseResponse from(WorkoutExercise workoutExercise) {
      List<WorkoutSetResponse> workoutSetResponses = workoutExercise.getWorkoutSets().stream()
          .map(WorkoutSetResponse::from)
          .collect(Collectors.toList());

      return new WorkoutExerciseResponse(
          workoutExercise.getId(),
          workoutExercise.getExercise().getName(), // 연관된 exercise의 이름 사용
          workoutExercise.getOrder(),
          workoutSetResponses
      );
    }
  }

  public record WorkoutSetResponse(
      Long workoutSetId,
      int order,
      BigDecimal weight,
      int reps,
      Set<FeedbackResponse> feedbacks
  ) {
    public static WorkoutSetResponse from(WorkoutSet workoutSet) {
      Set<FeedbackResponse> feedbackResponses = workoutSet.getFeedbacks().stream()
          .map(FeedbackResponse::from)
          .collect(Collectors.toSet());

      return new WorkoutSetResponse(
          workoutSet.getId(),
          workoutSet.getOrder(),
          workoutSet.getWeight(),
          workoutSet.getReps(),
          feedbackResponses
      );
    }
  }

  public record FeedbackResponse(
      Long feedbackId,
      String authorName,
      String content
  ) {
    public static FeedbackResponse from(Feedback feedback) {
      return new FeedbackResponse(
          feedback.getId(),
          feedback.getAuthor().getName(), // 연관된 author의 이름 사용
          feedback.getContent()
      );
    }
  }
}