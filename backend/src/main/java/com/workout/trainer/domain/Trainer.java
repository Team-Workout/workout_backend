package com.workout.trainer.domain;

import com.workout.member.domain.Member;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
  private List<Award> awards;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private List<Certification> certifications;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private List<Education> educations;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private List<Workexperience> workexperiences;

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private Set<TrainerSpecialty> trainerSpecialties;

  private String phoneNumber;
  private String introduction;
}
