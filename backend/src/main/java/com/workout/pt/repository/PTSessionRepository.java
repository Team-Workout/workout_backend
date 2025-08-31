package com.workout.pt.repository;

import com.workout.pt.domain.session.PTSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PTSessionRepository extends JpaRepository<PTSession, Long> {

  @Query("SELECT ps FROM PTSession ps " +
      "JOIN FETCH ps.workoutLog wl " +
      "JOIN FETCH wl.member m " +
      "WHERE m.id = :userId")
  Page<PTSession> findByMemberIdWithDetails(@Param("userId") Long userId, Pageable pageable);
}
