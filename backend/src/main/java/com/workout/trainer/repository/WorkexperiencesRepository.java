package com.workout.trainer.repository;

import com.workout.trainer.domain.Workexperience;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkexperiencesRepository extends JpaRepository<Workexperience, Long> {

  List<Workexperience> findAllByTrainerId(Long trainerId);

  void deleteAllByTrainerId(Long trainerId);
}
