package com.workout.trainer.repository;

import com.workout.trainer.domain.Trainer;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository  extends JpaRepository<Trainer, Long> {

  @Query("SELECT DISTINCT t FROM Trainer t " +
      "LEFT JOIN FETCH t.awards " +
      "LEFT JOIN FETCH t.certifications " +
      "LEFT JOIN FETCH t.educations " +
      "LEFT JOIN FETCH t.workexperiences " +
      "LEFT JOIN FETCH t.specialties ts JOIN FETCH ts.specialty " +
      "WHERE t.id = :trainerId")
  Optional<Trainer> findProfileById(@Param("trainerId") Long trainerId);

  @Query("SELECT DISTINCT t FROM Trainer t " +
      "LEFT JOIN FETCH t.awards " +
      "LEFT JOIN FETCH t.certifications " +
      "LEFT JOIN FETCH t.educations " +
      "LEFT JOIN FETCH t.workexperiences " +
      "LEFT JOIN FETCH t.specialties ts JOIN FETCH ts.specialty " +
      "WHERE t.gym.id = :gymId")
  List<Trainer> findProfilesByGymId(@Param("gymId") Long gymId);
}
