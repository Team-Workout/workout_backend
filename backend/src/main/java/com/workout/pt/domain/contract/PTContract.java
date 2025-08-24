package com.workout.pt.domain.contract;

import com.workout.global.BaseEntity;
import com.workout.gym.domain.Gym;
import com.workout.member.domain.Member;
import com.workout.trainer.domain.Trainer;
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
import jakarta.persistence.Version;
import java.time.LocalDate;
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
@Table(name = "pt_contract")
public class PTContract extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Version
  private Integer version;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "gym_id", nullable = false, updatable = false)
  private Gym gym;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "application_id", nullable = false, updatable = false)
  private PTApplication application;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false, updatable = false)
  private Member member;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_id", nullable = false, updatable = false)
  private Trainer trainer;

  @Enumerated(EnumType.STRING)
  private PTContractStatus status;

  private Long price;
  private LocalDate paymentDate;
  private LocalDate startDate;
  private Long totalSessions;
  private Long remainingSessions;

  boolean allowBodyCompView = false;
  boolean allowPhotoview = false;
}
