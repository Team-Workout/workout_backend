package com.workout.trainer.repository;

import com.workout.trainer.domain.Workexperiences;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkexperiencesRepository extends JpaRepository<Workexperiences, Long> {

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Workexperiences w WHERE w.trainer.id = :trainerId")
  void deleteAllByTrainerId(Long trainerId);

  List<Workexperiences> findByTrainerId(Long trainerId);
}
