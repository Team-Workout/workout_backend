package com.workout.trainer.repository;

import com.workout.trainer.domain.Trainer;
import com.workout.trainer.dto.TrainerProfileDto;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TrainerRepository extends JpaRepository<Trainer, Long> {

  List<Trainer> findAllByGymId(Long gymId);

  @EntityGraph(attributePaths = {"awards", "certifications", "educations", "workexperiences"})
  @Query("SELECT t FROM Trainer t WHERE t.gym.id = :gymId")
  List<Trainer> findAllByGymIdWithDetails(@Param("gymId") Long gymId);

  @Query("SELECT NEW com.workout.trainer.dto.TrainerProfileDto(" +
      "    t.id, t.name, t.email, t.introduction, " +
      "    a.id, a.awardName, a.awardDate, a.awardPlace, " +
      "    c.id, c.certificationName, c.issuingOrganization, c.acquisitionDate, " +
      "    e.id, e.schoolName, e.educationName, e.degree, e.startDate, e.endDate, " +
      "    w.id, w.workName, w.workPlace, w.workPosition, w.workStart, w.workEnd" +
      ") " +
      "FROM Trainer t " +
      "LEFT JOIN Award a ON t.id = a.trainer.id " +
      "LEFT JOIN Certification c ON t.id = c.trainer.id " +
      "LEFT JOIN Education e ON t.id = e.trainer.id " +
      "LEFT JOIN Workexperience w ON t.id = w.trainer.id " +
      "WHERE t.gym.id = :gymId")
  List<TrainerProfileDto> findTrainerProfilesByGymIdAsFlatDto(@Param("gymId") Long gymId);
}