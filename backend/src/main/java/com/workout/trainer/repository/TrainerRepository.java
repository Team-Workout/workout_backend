package com.workout.trainer.repository;

import com.workout.trainer.domain.Trainer;
import com.workout.trainer.dto.TrainerProfileDto;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

  @Query("SELECT DISTINCT t FROM Trainer t " +
      "LEFT JOIN FETCH t.awards " +
      "LEFT JOIN FETCH t.certifications " +
      "LEFT JOIN FETCH t.educations " +
      "LEFT JOIN FETCH t.workexperiences " +
      "LEFT JOIN FETCH t.trainerSpecialties ts " +
      "LEFT JOIN FETCH ts.specialty " +
      "WHERE t.id = :trainerId")
  Optional<Trainer> findByIdWithDetails(@Param("trainerId") Long trainerId);


  Page<Trainer> findAllByGymId(Long gymId, Pageable pageable);
}