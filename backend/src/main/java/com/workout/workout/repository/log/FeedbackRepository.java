package com.workout.workout.repository.log;

import com.workout.workout.domain.log.Feedback;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

  @Query("SELECT f FROM Feedback f WHERE f.workoutLog.id = :logId OR f.workoutExercise.id IN :exerciseIds OR f.workoutSet.id IN :setIds")
  List<Feedback> findByWorkoutElements(@Param("logId") Long logId,
      @Param("exerciseIds") List<Long> exerciseIds, @Param("setIds") List<Long> setIds);

  @Query("SELECT f FROM Feedback f " +
      "WHERE f.workoutLog.id IN :workoutLogIds " +
      "OR f.workoutExercise.id IN :exerciseIds " +
      "OR f.workoutSet.id IN :setIds")
  List<Feedback> findByWorkoutElements(@Param("workoutLogIds") List<Long> workoutLogIds,
      @Param("exerciseIds") List<Long> exerciseIds,
      @Param("setIds") List<Long> setIds);

  @Modifying
  @Query("DELETE FROM Feedback f WHERE f.workoutLog.id = :workoutLogId")
  void deleteAllByWorkoutLogId(@Param("workoutLogId") Long workoutLogId);

  @Modifying
  @Query("DELETE FROM Feedback f WHERE f.workoutExercise.id IN :exerciseIds")
  void deleteAllByWorkoutExerciseIdIn(@Param("exerciseIds") List<Long> exerciseIds);

  @Modifying
  @Query("DELETE FROM Feedback f WHERE f.workoutSet.id IN :setIds")
  void deleteAllByWorkoutSetIdIn(@Param("setIds") List<Long> setIds);
}
