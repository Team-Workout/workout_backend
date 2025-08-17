package com.workout.workout.repository.routine;

import com.workout.workout.domain.routine.Routine;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoutineRepository extends JpaRepository<Routine, Long> {

}
