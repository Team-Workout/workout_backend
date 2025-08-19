package com.workout.pt.domain;

import com.workout.global.BaseEntity;
import com.workout.member.domain.Member;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SuperBuilder
@Entity
@Table(name = "pt_appointment_change_request")
public class PTAppointmentChangeRequest extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 어떤 원본 예약을 변경하려는지 참조
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "appointment_id", nullable = false, updatable = false)
  private PTAppointment appointment;

  // 누가 요청했는지
  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "member_id", nullable = false, updatable = false)
  private Member requester;

  // 기존 시간 정보 (기록용)
  @Column(nullable = false)
  private LocalDateTime originalStartTime;

  // 새로 제안하는 시간
  @Column(nullable = false)
  private LocalDateTime proposedStartTime;
  @Column(nullable = false)
  private LocalDateTime proposedEndTime;

  // 변경 요청 사유
  private String reason;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private ChangeRequestStatus status;
}