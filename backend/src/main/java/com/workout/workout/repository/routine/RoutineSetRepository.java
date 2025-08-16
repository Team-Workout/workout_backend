package com.workout.workout.repository.routine;

import com.workout.workout.domain.routine.RoutineSet;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutineSetRepository extends JpaRepository<RoutineSet, Long> {
  List<RoutineSet> findAllByRoutineExerciseIdInOrderByOrderAsc(List<Long> routineExerciseIds);
  void deleteAllByRoutineExerciseIdIn(List<Long> routineExerciseIds);
}
