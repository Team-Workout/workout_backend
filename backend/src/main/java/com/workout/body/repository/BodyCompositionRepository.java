package com.workout.body.repository;

import com.workout.body.domain.BodyComposition;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BodyCompositionRepository extends JpaRepository<BodyComposition, Long> {

  Page<BodyComposition> findByMemberIdAndMeasurementDateBetweenOrderByMeasurementDateDesc(
      Long memberId, LocalDate startDate, LocalDate endDate, Pageable pageable);

  Optional<BodyComposition> findByIdAndMemberId(Long id, Long memberId);
  Optional<BodyComposition> findByMemberIdAndMeasurementDate(Long memberId, LocalDate measurementDate);
}