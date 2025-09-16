package com.workout.trainer.domain;

import com.workout.member.domain.Member;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.BatchSize;

@Entity
@DiscriminatorValue("TRAINER")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
public class Trainer extends Member {

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private Set<Award> awards;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private Set<Certification> certifications;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private Set<Education> educations;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private Set<Workexperience> workExperiences;

  // TrainerSpecialty는 다대다 관계이므로 별도 처리가 필요할 수 있습니다.
  // 여기서는 간단히 OneToMany로 표현합니다.
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private Set<TrainerSpecialty> trainerSpecialties;


  private String phoneNumber;
  private String introduction;
}
