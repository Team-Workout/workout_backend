package com.workout.workout.repository.routine;

import com.workout.workout.domain.routine.Routine;
import com.workout.workout.dto.routine.RoutineResponse;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

  List<Routine> findAllRoutinesByMemberId(Long userId);
}
