package com.workout.trainer.repository;

import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.TrainerSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

public interface TrainerSpecialtyRepository extends JpaRepository<TrainerSpecialty, Long> {
  void deleteAllByTrainerId(Long trainerId);

  @Query("SELECT s FROM Specialty s JOIN TrainerSpecialty ts ON s.id = ts.specialty.id WHERE ts.trainer.id = :trainerId")
  Set<Specialty> findSpecialtiesByTrainerId(@Param("trainerId") Long trainerId);
}