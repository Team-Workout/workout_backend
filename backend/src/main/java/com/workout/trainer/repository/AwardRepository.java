package com.workout.trainer.repository;

import com.workout.trainer.domain.Award;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AwardRepository extends JpaRepository<Award, Long> {

  // 프로필 조회 시 사용
  List<Award> findAllByTrainerId(Long trainerId);

  // 프로필 수정 및 삭제 시 사용
  void deleteAllByTrainerId(Long trainerId);
}
