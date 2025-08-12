package com.workout.workout.repository;

import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.routine.Routine;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoutineRepository extends JpaRepository<Routine, Long> {
  @Query("SELECT ro FROM Routine ro " +
      "LEFT JOIN FETCH ro.routineExercises re " +
      "LEFT JOIN FETCH re.routine " +
      "WHERE ro.id = :id")
  Optional<Routine> findByIdWithDetails(@Param("id") Long id);
}
