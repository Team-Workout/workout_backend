package com.workout.workout.repository;

import com.workout.workout.domain.log.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

}
