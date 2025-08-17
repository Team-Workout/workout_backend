package com.workout.trainer.repository;

import com.workout.trainer.domain.Certification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
  List<Certification> findAllByTrainerId(Long trainerId);

  void deleteAllByTrainerId(Long trainerId);
}
