package com.workout.body.repository;

import com.workout.body.domain.BodyComposition;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BodyCompositionRepository extends JpaRepository<BodyComposition, Long> {

  List<BodyComposition> findByMemberId(Long memberId);

  Optional<BodyComposition> findByIdAndMemberId(Long id, Long memberId);
}