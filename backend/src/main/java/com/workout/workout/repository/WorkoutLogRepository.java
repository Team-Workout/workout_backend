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


  @Query("SELECT DISTINCT wl FROM WorkoutLog wl " +
      "LEFT JOIN FETCH wl.user u " + // 사용자 정보도 함께 가져옴
      "LEFT JOIN FETCH wl.feedbacks f_wl " +
      "LEFT JOIN FETCH wl.workoutExercises we " +
      "LEFT JOIN FETCH we.exercise e " +
      "LEFT JOIN FETCH we.workoutSets ws " +
      "LEFT JOIN FETCH ws.feedbacks f_ws " +
      "WHERE wl.id = :id")
  Optional<WorkoutLog> findWorkoutLogGraph(@Param("id") Long id);

  boolean existsByIdAndUserId(Long id, Long userId);
}


