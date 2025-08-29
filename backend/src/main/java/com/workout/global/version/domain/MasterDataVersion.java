package com.workout.global.version.domain;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MasterDataVersion {

  @Id
  @Column(name = "data_type")
  private String dataType;

  @Column(nullable = false)
  private Long version;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public MasterDataVersion(String dataType, Long version) {
    this.dataType = dataType;
    this.version = version;
  }

  public void updateVersion(Long newVersion) {
    this.version = newVersion;
  }
}