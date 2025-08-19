package com.workout.pt.domain;

public enum PTContractStatus {
  ACTIVE,      // PT 진행 중
  COMPLETED,   // 모든 세션 완료
  FROZEN,      // 일시 정지 (회원 요청 등)
  CANCELLED,   // 중도 해지/환불
  EXPIRED      // 기간 만료
}