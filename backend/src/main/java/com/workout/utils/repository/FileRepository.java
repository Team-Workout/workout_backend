package com.workout.utils.repository;

import com.workout.utils.domain.UserFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<UserFile, Long> {

}
