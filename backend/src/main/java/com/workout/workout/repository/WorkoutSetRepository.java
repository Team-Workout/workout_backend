package com.workout.workout.repository;

import com.workout.workout.domain.log.WorkoutSet;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {
  @Modifying
  @Query("DELETE FROM WorkoutSet ws WHERE ws.workoutExercise.id IN :exerciseIds")
  void deleteAllByWorkoutExerciseIdIn(@Param("exerciseIds") List<Long> exerciseIds);

  @Query("SELECT ws.id FROM WorkoutSet ws WHERE ws.workoutExercise.id IN :exerciseIds")
  List<Long> findIdsByWorkoutExerciseIdIn(@Param("exerciseIds") List<Long> exerciseIds);
}
