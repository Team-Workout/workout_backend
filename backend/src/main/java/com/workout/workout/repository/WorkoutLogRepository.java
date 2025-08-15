package com.workout.workout.repository;

import com.workout.workout.domain.log.WorkoutLog;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
  @Query("SELECT wl FROM WorkoutLog wl " +
      "LEFT JOIN FETCH wl.workoutExercises we " +
      "LEFT JOIN FETCH we.exercise " +
      "WHERE wl.id = :id")
  Optional<WorkoutLog> findByIdWithDetails(@Param("id") Long id);

  void deleteAllByWorkoutExerciseIdIn(List<Long> exerciseIds);
}


