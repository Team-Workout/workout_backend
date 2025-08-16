package com.workout.workout.repository.log;

import com.workout.workout.domain.log.WorkoutSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkoutSetRepository extends JpaRepository<WorkoutSet, Long> {
  List<WorkoutSet> findAllByWorkoutExerciseIdInOrderByOrderAsc(List<Long> exerciseIds);

  @Query("SELECT ws.id FROM WorkoutSet ws WHERE ws.workoutExercise.id IN :exerciseIds")
  List<Long> findIdsByWorkoutExerciseIdIn(@Param("exerciseIds") List<Long> exerciseIds);

  @Modifying
  @Query("DELETE FROM WorkoutSet ws WHERE ws.workoutExercise.id IN :exerciseIds")
  void deleteAllByWorkoutExerciseIdIn(@Param("exerciseIds") List<Long> exerciseIds);
}

