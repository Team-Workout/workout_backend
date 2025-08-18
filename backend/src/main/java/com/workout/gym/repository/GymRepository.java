package com.workout.gym.repository;


import com.workout.gym.domain.Gym;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GymRepository extends JpaRepository<Gym, Long> {

  Optional<Gym> findById(Long id);

  boolean existsById(Long id);
}
