package com.workout.trainer.repository;

import com.workout.trainer.domain.Workexperiences;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkexperiencesRepository extends JpaRepository<Workexperiences, Long> {
  List<Workexperiences> findAllByTrainerId(Long trainerId);

  void deleteAllByTrainerId(Long trainerId);
}
