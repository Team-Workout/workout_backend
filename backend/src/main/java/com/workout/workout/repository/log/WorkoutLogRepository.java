package com.workout.workout.repository.log;

import com.workout.workout.domain.log.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {
  boolean existsByIdAndMemberId(Long logId, Long memberId);
}


