package com.workout.trainer.repository;

import com.workout.trainer.domain.Specialty;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {
  Set<Specialty> findByNameIn(Set<String> names);
}