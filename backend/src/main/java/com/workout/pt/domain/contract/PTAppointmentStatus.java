package com.workout.pt.domain.contract;

public enum PTAppointmentStatus {
  MEMBER_REQUESTED,   // 회원이 예약을 요청한 상태
  SCHEDULED,          // 트레이너가 확정한 상태 (기존 예약됨)
  COMPLETED,          // 완료됨
  CANCELLED,          // 취소됨
  CHANGE_REQUESTED,   // 회원이 변경을 요청한 상태
  TRAINER_CHANGE_REQUESTED; // 트레이너가 변경을 요청한 상태
}