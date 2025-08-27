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
  private List<Award> awards = new ArrayList<>();

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private List<Certification> certifications = new ArrayList<>();

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private List<Education> educations = new ArrayList<>();

  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private List<Workexperience> workexperiences = new ArrayList<>();

  // TrainerSpecialty는 다대다 관계이므로 별도 처리가 필요할 수 있습니다.
  // 여기서는 간단히 OneToMany로 표현합니다.
  @BatchSize(size = 100)
  @OneToMany(mappedBy = "trainer", fetch = FetchType.LAZY)
  private Set<TrainerSpecialty> trainerSpecialties = new HashSet<>();


  private String phoneNumber;
  private String introduction;
}
