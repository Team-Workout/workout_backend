package com.workout.workout.repository;

import com.workout.workout.domain.exercise.ExerciseTargetMuscle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ExerciseTargetMuscleRepository extends JpaRepository<ExerciseTargetMuscle, Long> {

}
