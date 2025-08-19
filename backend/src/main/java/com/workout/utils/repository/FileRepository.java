package com.workout.utils.repository;

import java.util.List;

import com.workout.utils.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<File, Long> {

    List<File> findByMemberIdAndCategory(Long memberId, String fileCategory);

}
