package com.workout.workout.domain.log;

import com.workout.global.Auditable;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutFeedback implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_set_id")
  private WorkoutSet workoutSet;

  @Column(nullable = false)
  private Long commenterId; // 피드백 작성자의 ID (User ID 또는 Trainer ID)

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private CommenterType commenterType; // 작성자 타입

  @Lob // 긴 텍스트를 위한 설정
  private String comment;

  @Builder
  public WorkoutFeedback(Long commenterId, CommenterType commenterType, String comment) {
    this.commenterId = commenterId;
    this.commenterType = commenterType;
    this.comment = comment;
  }
}
