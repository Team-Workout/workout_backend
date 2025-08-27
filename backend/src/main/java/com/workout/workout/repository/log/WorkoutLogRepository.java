package com.workout.workout.repository.log;

import com.workout.workout.domain.log.WorkoutLog;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, Long> {

  boolean existsByIdAndMemberId(Long logId, Long memberId);

  List<WorkoutLog> findAllByMemberIdAndWorkoutDateBetweenOrderByWorkoutDateDesc(Long userId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}


