package com.workout.trainer.repository;

import com.workout.trainer.domain.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository  extends JpaRepository<Trainer, Long> {
}
