package com.workout.workout.repository;

import com.workout.workout.domain.log.WorkoutExercise;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {

  @Modifying
  @Query("DELETE FROM WorkoutExercise we WHERE we.workoutLog.id = :logId")
  void deleteAllByWorkoutLogId(@Param("logId") Long logId);

  @Query("SELECT we.id FROM WorkoutExercise we WHERE we.workoutLog.id = :logId")
  List<Long> findIdsByWorkoutLogId(@Param("logId") Long logId);
}
