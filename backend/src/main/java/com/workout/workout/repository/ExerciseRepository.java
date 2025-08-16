package com.workout.workout.repository;

import com.workout.workout.domain.exercise.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {
  List<Exercise> findAllByIdIn(List<Long> ids);
}
