package com.workout.workout.repository;

import com.workout.workout.domain.log.Feedback;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
  @Modifying @Query("DELETE FROM Feedback f WHERE f.workoutLog.id = :logId")
  void deleteAllByWorkoutLogId(@Param("logId") Long logId);

  @Modifying @Query("DELETE FROM Feedback f WHERE f.workoutExercise.id in :exerciseIds")
  void deleteAllByWorkoutExerciseIdIn(@Param("exerciseIds") List<Long> exerciseIds);

  @Modifying
  @Query("DELETE FROM Feedback f WHERE f.workoutSet.id in :setIds")
  void deleteAllByWorkoutSetIdIn(@Param("setIds") List<Long> setIds);
}
