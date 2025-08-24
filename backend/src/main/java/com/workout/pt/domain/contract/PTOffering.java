package com.workout.pt.domain.contract;

import com.workout.global.BaseEntity;
import com.workout.gym.domain.Gym;
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
import jakarta.persistence.Lob;
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
@Table(name = "pt_offering")
public class PTOffering extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "trainer_id", nullable = false, updatable = false)
  private Trainer trainer; // 이 상품을 등록한 트레이너

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "gym_id", nullable = false, updatable = false)
  private Gym gym;

  @Column(nullable = false)
  private String title; // 예: "3개월 바디프로필 완성반"

  @Lob // 긴 설명 텍스트
  private String description;

  @Column(nullable = false)
  private Long price; // 가격

  @Column(nullable = false)
  private Long totalSessions; // 총 세션 횟수

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private PTOfferingStatus status; // 상태 (신청 가능, 마감 등)
}