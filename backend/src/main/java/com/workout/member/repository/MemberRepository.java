package com.workout.member.repository;

import com.workout.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByEmail(String email);

  boolean existsByName(String name);

  Optional<Member> findByEmail(String email);

  @Query("SELECT m.fcmToken FROM Member m WHERE m.id = :memberId")
  String findFcmTokenById(@Param("memberId") Long memberId);

  Optional<Member> findByFcmToken(String invalidToken);
}
