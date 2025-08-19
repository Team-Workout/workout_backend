package com.workout.pt.repository;

import com.workout.pt.domain.PTApplication;
import com.workout.pt.domain.PTApplicationStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PTApplicationRepository extends JpaRepository<PTApplication, Long> {

  List<PTApplication> findByTrainerIdAndStatus(Long trainerId, PTApplicationStatus ptApplicationStatus);

  List<PTApplication> findByMemberIdAndStatus(Long id, PTApplicationStatus ptApplicationStatus);
}
