package com.workout.member.repository;

import com.workout.member.domain.Member;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

  boolean existsByEmail(String email);

  boolean existsByName(String name);

  Optional<Member> findByEmail(String email);

  List<Member> findByIdIn(List<Long> ids);
}
