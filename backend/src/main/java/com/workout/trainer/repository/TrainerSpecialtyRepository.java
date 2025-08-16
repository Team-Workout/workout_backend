package com.workout.trainer.repository;

import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.TrainerSpecialty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Set;

public interface TrainerSpecialtyRepository extends JpaRepository<TrainerSpecialty, Long> {
  // 특정 트레이너의 모든 전문 분야 관계를 삭제
  void deleteAllByTrainerId(Long trainerId);

  // 특정 트레이너의 Specialty 엔티티 목록을 조회 (getProfile에서 사용)
  @Query("SELECT s FROM Specialty s JOIN TrainerSpecialty ts ON s.id = ts.specialty.id WHERE ts.trainer.id = :trainerId")
  Set<Specialty> findSpecialtiesByTrainerId(@Param("trainerId") Long trainerId);
}