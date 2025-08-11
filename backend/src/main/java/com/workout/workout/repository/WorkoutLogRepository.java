package com.workout.workout.repository;

import com.workout.workout.domain.log.WorkoutLog;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
  @Query("SELECT wl FROM WorkoutLog wl " +
      "JOIN FETCH wl.workoutSets ws " +
      "JOIN FETCH ws.exercise " +
      "WHERE wl.id = :id")
  Optional<WorkoutLog> findByIdWithSets(@Param("id") Long id);
}


