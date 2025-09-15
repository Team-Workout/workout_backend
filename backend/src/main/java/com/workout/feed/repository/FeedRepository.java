package com.workout.feed.repository;

import com.workout.feed.domain.Feed;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FeedRepository extends JpaRepository<Feed, Long> {

  @Query("SELECT f FROM Feed f JOIN FETCH f.member WHERE f.id = :feedId")
  Optional<Feed> findByIdWithMember(@Param("feedId") Long feedId);

  // 1. 최초 로딩 (가장 최신 피드부터)
  @Query("SELECT f FROM Feed f JOIN FETCH f.member " +
      "WHERE f.gym.id = :gymId " +
      "ORDER BY f.id DESC")
  List<Feed> findByGymIdFirstPage(@Param("gymId") Long gymId, Pageable pageable);

  // 2. 아래로 스크롤 (더 오래된 피드)
  @Query("SELECT f FROM Feed f JOIN FETCH f.member " +
      "WHERE f.gym.id = :gymId AND f.id < :lastFeedId " +
      "ORDER BY f.id DESC")
  List<Feed> findOlderFeedsByGymIdWithCursor(@Param("gymId") Long gymId,
      @Param("lastFeedId") Long lastFeedId,
      Pageable pageable);

  // 🟢 3. 위로 새로고침 (더 최신 피드)
  @Query("SELECT f FROM Feed f JOIN FETCH f.member " +
      "WHERE f.gym.id = :gymId AND f.id > :firstFeedId " +
      "ORDER BY f.id DESC")
  List<Feed> findNewerFeedsByGymIdWithCursor(@Param("gymId") Long gymId,
      @Param("firstFeedId") Long firstFeedId);

}
