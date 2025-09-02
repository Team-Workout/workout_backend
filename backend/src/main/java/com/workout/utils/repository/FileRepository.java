package com.workout.utils.repository;

import com.workout.utils.domain.ImagePurpose;
import com.workout.utils.domain.UserFile;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FileRepository extends JpaRepository<UserFile, Long> {
  List<UserFile> findByMemberIdAndPurposeAndRecordDateBetweenOrderByRecordDateDesc(
      Long memberId, ImagePurpose purpose, LocalDate startDate, LocalDate endDate
  );
}
