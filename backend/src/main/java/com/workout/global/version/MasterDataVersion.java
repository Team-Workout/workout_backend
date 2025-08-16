package com.workout.global.version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MasterDataVersion {

  @Id
  @Column(name = "data_type")
  private String dataType; // "EXERCISE", "MUSCLE" 등

  @Column(nullable = false)
  private String version;

  @UpdateTimestamp // 엔티티가 수정될 때마다 자동으로 현재 시간 저장
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  @Builder
  public MasterDataVersion (String dataType, String version) {
    this.dataType = dataType;
    this.version = version;
  }

  // 버전 업데이트를 위한 편의 메소드
  public void updateVersion(String newVersion) {
    this.version = newVersion;
  }
}