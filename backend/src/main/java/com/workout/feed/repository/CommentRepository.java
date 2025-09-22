package com.workout.feed.repository;

import com.workout.feed.domain.Comment;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {

  @Modifying
  @Query("DELETE FROM Comment c WHERE c.feed.id = :feedId")
  void deleteAllByFeedId(@Param("feedId") Long feedId);

  List<Comment> findByParentId(Long parentId);

  @Query(value = "SELECT c FROM Comment c JOIN FETCH c.member m WHERE c.feed.id = :feedId AND c.parent IS NULL",
      countQuery = "SELECT count(c) FROM Comment c WHERE c.feed.id = :feedId AND c.parent IS NULL")
  Page<Comment> findByFeedIdAndParentIsNull(@Param("feedId") Long feedId, Pageable pageable);
}

