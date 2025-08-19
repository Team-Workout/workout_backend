package com.workout.pt.repository;

import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PTApplicationRepository extends JpaRepository<PTApplication, Long> {

  List<PTApplication> findByTrainerIdAndStatus(Long trainerId,
      PTApplicationStatus ptApplicationStatus);

  List<PTApplication> findByMemberIdAndStatus(Long id, PTApplicationStatus ptApplicationStatus);

  @Query("SELECT pa FROM PTApplication pa JOIN pa.offering o WHERE o.trainer.id = :trainerId AND pa.status = :status")
  List<PTApplication> findPendingApplicationsByTrainerId(@Param("trainerId") Long trainerId, @Param("status") PTApplicationStatus status);
}
