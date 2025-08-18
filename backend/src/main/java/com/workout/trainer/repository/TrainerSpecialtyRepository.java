package com.workout.trainer.repository;

import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.TrainerSpecialty;
import com.workout.trainer.dto.TrainerSpecialtyDto;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerSpecialtyRepository extends JpaRepository<TrainerSpecialty, Long> {

  void deleteAllByTrainerId(Long trainerId);

  @Query("SELECT s FROM Specialty s JOIN TrainerSpecialty ts ON s.id = ts.specialty.id WHERE ts.trainer.id = :trainerId")
  Set<Specialty> findSpecialtiesByTrainerId(@Param("trainerId") Long trainerId);

  @Query("SELECT NEW com.workout.trainer.dto.TrainerSpecialtyDto(ts.trainer.id, s.name) " +
      "FROM TrainerSpecialty ts JOIN ts.specialty s " +
      "WHERE ts.trainer.id IN :trainerIds")
  List<TrainerSpecialtyDto> findSpecialtiesByTrainerIds(@Param("trainerIds") List<Long> trainerIds);

}