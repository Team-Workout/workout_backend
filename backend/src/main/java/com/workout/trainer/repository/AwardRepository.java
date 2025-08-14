package com.workout.trainer.repository;

import com.workout.trainer.domain.Award;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AwardRepository  extends JpaRepository<Award, Long> {

  @Modifying(clearAutomatically = true)
  @Query("DELETE FROM Award a WHERE a.trainer.id = :trainerId")
  void deleteAllByTrainerId(Long trainerId);

  List<Award> findByTrainerId(Long trainerId);
}
