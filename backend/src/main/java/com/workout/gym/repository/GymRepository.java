package com.workout.gym.repository;


import com.workout.gym.domain.Gym;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {
    Optional<Gym> findById(Long id);
    boolean existsById(Long id);
}
