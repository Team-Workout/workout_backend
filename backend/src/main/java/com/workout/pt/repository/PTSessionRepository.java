package com.workout.pt.repository;

import com.workout.pt.domain.session.PTSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.session.SessionRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PTSessionRepository extends JpaRepository<PTSession, Long> {
}
