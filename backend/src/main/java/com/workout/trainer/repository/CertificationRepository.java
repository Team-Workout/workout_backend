package com.workout.trainer.repository;

import com.workout.trainer.domain.Certification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Certification c WHERE c.trainer.id = :trainerId")
  void deleteAllByTrainerId(Long trainerId);

  List<Certification> findByTrainerId(Long trainerId);
}
