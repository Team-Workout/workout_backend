package com.workout.workout.repository;

import com.workout.workout.domain.exercise.Exercise;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ExerciseRepository extends JpaRepository<Exercise, Long> {

  List<Exercise> findAllByIdIn(List<Long> ids);

  @Query("SELECT DISTINCT e FROM Exercise e " +
      "LEFT JOIN FETCH e.mappedMuscles mm " +
      "LEFT JOIN FETCH mm.targetMuscle")
  List<Exercise> findAllWithMuscles();
}
