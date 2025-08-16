package com.workout.global;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import java.time.Instant;

public class AuditListener {

  @PrePersist // 엔티티가 처음 저장될 때 호출
  public void setCreatedAt(Object entity) {
    Instant now = Instant.now();

    // 만약 엔티티가 Creatable 인터페이스를 구현했다면
    if (entity instanceof Creatable) {
      ((Creatable) entity).setCreatedAt(now);
    }

    // 만약 엔티티가 Auditable 인터페이스를 구현했다면 (생성 시에는 updatedAt도 createdAt과 동일하게 설정)
    if (entity instanceof Auditable) {
      ((Auditable) entity).setUpdatedAt(now);
    }
  }

  @PreUpdate // 엔티티가 업데이트될 때 호출
  public void setUpdatedAt(Object entity) {
    // 만약 엔티티가 Auditable 인터페이스를 구현했다면
    if (entity instanceof Auditable) {
      ((Auditable) entity).setUpdatedAt(Instant.now());
    }
  }
}