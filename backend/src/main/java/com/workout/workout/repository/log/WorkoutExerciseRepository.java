package com.workout.workout.repository.log;

import com.workout.workout.domain.log.WorkoutExercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutExerciseRepository extends JpaRepository<WorkoutExercise, Long> {
  List<WorkoutExercise> findAllByWorkoutLogIdOrderByOrderAsc(Long workoutLogId);


  @Query("SELECT we.id FROM WorkoutExercise we WHERE we.workoutLog.id = :workoutLogId")
  List<Long> findIdsByWorkoutLogId(@Param("workoutLogId") Long workoutLogId);

  @Modifying
  @Query("DELETE FROM WorkoutExercise we WHERE we.workoutLog.id = :workoutLogId")
  void deleteAllByWorkoutLogId(@Param("workoutLogId") Long workoutLogId);
}
