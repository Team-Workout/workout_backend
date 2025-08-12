package com.workout.global;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.Instant;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;

@Getter
@MappedSuperclass
public abstract class AuditableEntity extends CreatedAtEntity { // CreatedAtEntity를 상속

  @LastModifiedDate
  @Column(name = "updated_at")
  private Instant updatedAt;
}