package com.workout.utils.repository;

import com.workout.member.domain.Member;
import com.workout.utils.domain.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<UserFile, Long> {

}
