package com.workout.feed.repository;

import com.workout.feed.domain.Like;
import com.workout.feed.domain.LikeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LikeRepository extends JpaRepository<Like, Long> {

  Optional<Like> findByMemberIdAndTargetTypeAndTargetId(Long userId, LikeType targetType,
      Long targetId);

  @Modifying
  @Query("DELETE FROM Like l WHERE l.targetType = :targetType AND l.targetId = :targetId")
  void deleteAllByTargetTypeAndTargetId(@Param("targetType") LikeType targetType,
      @Param("targetId") Long targetId);
}
