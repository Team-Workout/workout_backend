package com.workout.global;

import java.time.Instant;

public interface Auditable extends Creatable { // 생성일 인터페이스 상속
  void setUpdatedAt(Instant updatedAt);
}