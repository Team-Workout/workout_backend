package com.workout.global;

import jakarta.persistence.EntityListeners;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@EntityListeners(AuditingEntityListener.class)
public interface Auditable { // 이름은 아래에서 다시 추천해 드립니다.
}