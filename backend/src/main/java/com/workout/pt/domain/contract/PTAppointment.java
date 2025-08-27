package com.workout.pt.domain.contract;

import com.workout.global.BaseEntity;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
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
@Table(name = "pt_appointment", uniqueConstraints = {
    @UniqueConstraint(
        name = "uk_appointment_contract_starttime", // 제약조건 이름 (DB에서 식별하기 위함)
        columnNames = {"contract_id", "startTime"} // 유니크해야 할 컬럼들
    )
})
public class PTAppointment extends BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "contract_id", nullable = false, updatable = false)
  private PTContract contract;

  @Enumerated(EnumType.STRING)
  private PTAppointmentStatus status; // 예약 상태 추가

  @Column(length = 500)
  private String cancellationReason; // 취소 사유

  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private LocalDateTime proposedStartTime;
  private LocalDateTime proposedEndTime;

  public void changeStatus(PTAppointmentStatus newStatus) {
    if (this.status == PTAppointmentStatus.COMPLETED) {
      throw new IllegalStateException("이미 완료된 예약의 상태는 변경할 수 없습니다.");
    }
    if (this.status == PTAppointmentStatus.CANCELLED) {
      throw new IllegalStateException("이미 취소된 예약입니다.");
    }

    this.status = newStatus;
  }

}
