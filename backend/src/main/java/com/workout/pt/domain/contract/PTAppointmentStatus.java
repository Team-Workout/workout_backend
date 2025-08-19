package com.workout.pt.domain.contract;

public enum PTAppointmentStatus {
  SCHEDULED,          // 예약 확정
  COMPLETED,          // 수업 완료
  CANCELLED_BY_MEMBER, // 회원이 취소
  CANCELLED_BY_TRAINER,// 트레이너가 취소
  NO_SHOW             // 회원이 나타나지 않음
}