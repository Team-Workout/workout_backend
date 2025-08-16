package com.workout.trainer.repository;

import com.workout.trainer.domain.Education;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EducationRepository extends JpaRepository<Education, Long> {
  List<Education> findAllByTrainerId(Long trainerId);

  void deleteAllByTrainerId(Long trainerId);
}
