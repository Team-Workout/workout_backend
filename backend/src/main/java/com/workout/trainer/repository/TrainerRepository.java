package com.workout.trainer.repository;

import com.workout.trainer.domain.Trainer;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

  List<Trainer> findAllByGymId(Long gymId);

  boolean existsByEmail(String email);
}
