package com.workout.trainer.repository;

import com.workout.trainer.domain.Certification;
import com.workout.trainer.domain.Specialty;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
  List<Certification> findAllByTrainerId(Long trainerId);

  void deleteAllByTrainerId(Long trainerId);
}
