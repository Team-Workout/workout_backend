package com.workout.pt.domain;

import com.workout.global.BaseEntity;
import com.workout.gym.domain.Gym;
import com.workout.member.domain.Member;
import com.workout.trainer.domain.Trainer;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "pt_application")
public class PTApplication extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "gym_id", nullable = false, updatable = false)
  Gym gym;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false, updatable = false)
  Member member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_id", nullable = false, updatable = false)
  Trainer trainer;

  @Enumerated(EnumType.STRING)
  @Column(name = "pt_application_status", nullable = false)
  PTApplicationStatus status;

  Long totalSessions;
}
