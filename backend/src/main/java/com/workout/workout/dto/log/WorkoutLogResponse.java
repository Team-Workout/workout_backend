package com.workout.workout.dto.log;

import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public record WorkoutLogResponse(
    Long workoutLogId,
    LocalDate workoutDate,
    Set<FeedbackResponse> feedbacks,
    List<WorkoutExerciseResponse> workoutExercises
) {

  public static WorkoutLogResponse from(WorkoutLog log, List<WorkoutExercise> exercises,
      List<WorkoutSet> sets, List<Feedback> feedbacks) {
    Map<Long, List<Feedback>> feedbackBySetId = feedbacks.stream()
        .filter(f -> f.getWorkoutSet() != null)
        .collect(Collectors.groupingBy(f -> f.getWorkoutSet().getId()));

    Map<Long, List<Feedback>> feedbackByExerciseId = feedbacks.stream()
        .filter(f -> f.getWorkoutExercise() != null)
        .collect(Collectors.groupingBy(f -> f.getWorkoutExercise().getId()));

    Set<FeedbackResponse> logFeedbacks = feedbacks.stream()
        .filter(f -> f.getWorkoutLog() != null)
        .map(FeedbackResponse::from)
        .collect(Collectors.toSet());

    Map<Long, List<WorkoutSetResponse>> setsByExerciseId = sets.stream()
        .collect(Collectors.groupingBy(
            set -> set.getWorkoutExercise().getId(),
            Collectors.mapping(set -> WorkoutSetResponse.from(set,
                    feedbackBySetId.getOrDefault(set.getId(), Collections.emptyList())),
                Collectors.toList())
        ));

    List<WorkoutExerciseResponse> exerciseResponses = exercises.stream()
        .map(ex -> WorkoutExerciseResponse.from(
            ex,
            setsByExerciseId.getOrDefault(ex.getId(), Collections.emptyList()),
            feedbackByExerciseId.getOrDefault(ex.getId(), Collections.emptyList())
        ))
        .toList();

    return new WorkoutLogResponse(log.getId(), log.getWorkoutDate(), logFeedbacks,
        exerciseResponses);
  }

  public record WorkoutExerciseResponse(
      Long workoutExerciseId,
      String exerciseName,
      int order,
      List<WorkoutSetResponse> workoutSets,
      Set<FeedbackResponse> feedbacks // 피드백 필드 추가
  ) {

    public static WorkoutExerciseResponse from(WorkoutExercise exercise,
        List<WorkoutSetResponse> sets, List<Feedback> feedbacks) {
      Set<FeedbackResponse> feedbackResponses = feedbacks.stream()
          .map(FeedbackResponse::from)
          .collect(Collectors.toSet());

      return new WorkoutExerciseResponse(
          exercise.getId(),
          exercise.getExercise().getName(),
          exercise.getOrder(),
          sets,
          feedbackResponses
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

    public static WorkoutSetResponse from(WorkoutSet workoutSet, List<Feedback> feedbacks) {
      Set<FeedbackResponse> feedbackResponses = feedbacks.stream()
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
          feedback.getAuthor().getName(),
          feedback.getContent()
      );
    }
  }
}