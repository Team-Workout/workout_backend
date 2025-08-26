package com.workout.workout.repository.routine;

import com.workout.workout.domain.routine.RoutineExercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineExerciseRepository extends JpaRepository<RoutineExercise, Long> {

  List<RoutineExercise> findAllByRoutineIdOrderByOrderAsc(Long routineId);

  void deleteAllByRoutineId(Long routineId);

  List<RoutineExercise> findAllByRoutineIdInOrderByOrderAsc(List<Long> routineIds);
}
