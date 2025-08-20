package com.workout.pt.repository;

import com.workout.pt.domain.contract.PTAppointment;
import com.workout.pt.domain.contract.PTAppointmentStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PTAppointmentRepository extends JpaRepository<PTAppointment, Long> {

  @Query("SELECT CASE WHEN COUNT(pa) > 0 THEN TRUE ELSE FALSE END " +
      "FROM PTAppointment pa " +
      "WHERE pa.contract.trainer.id = :trainerId " +
      "AND pa.status <> 'CANCELLED' " + // 취소된 예약은 겹쳐도 됨
      "AND pa.startTime < :endTime " +
      "AND pa.endTime > :startTime")
  boolean existsOverlappingAppointment(@Param("trainerId") Long trainerId,
      @Param("startTime") LocalDateTime startTime,
      @Param("endTime") LocalDateTime endTime);

  List<PTAppointment> findAllByContract_Member_IdAndStatus(Long memberId, PTAppointmentStatus status);

  List<PTAppointment> findAllByContract_Trainer_IdAndStatus(Long trainerId, PTAppointmentStatus status);
}
