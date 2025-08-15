package com.workout.body.repository;

import com.workout.body.domain.BodyComposition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BodyCompositionRepository extends JpaRepository<BodyComposition, Long> {
    List<BodyComposition> findByUserId(Long userId);

    Optional<BodyComposition> findByIdAndUserId(Long id, Long userId);


}