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
  List<Trainer> findAllByGymId(Long gymId);

  boolean existsByEmail(String email);
}
