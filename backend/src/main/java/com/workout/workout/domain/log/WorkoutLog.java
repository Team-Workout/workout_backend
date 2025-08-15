package com.workout.workout.domain.log;

import com.workout.global.AuditListener;
import com.workout.global.Auditable;
import com.workout.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditListener.class)
public class WorkoutLog implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @Column(name = "workout_date", nullable = false)
  private LocalDate workoutDate;

  @OneToMany(mappedBy = "workoutLog")
  @OrderBy("order ASC")
  private List<WorkoutExercise> workoutExercises = new ArrayList<>();

  @OneToMany(mappedBy = "workoutLog")
  private Set<Feedback> feedbacks = new HashSet<>();

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at")
  private Instant updatedAt;

  @Builder
  public WorkoutLog(User user, LocalDate workoutDate) {
    this.user = user;
    this.workoutDate = workoutDate;
  }

  //== 연관관계 편의 메소드 ==//
  public void addWorkoutExercise(WorkoutExercise workoutExercise) {
    this.workoutExercises.add(workoutExercise);
    // 부모-자식 관계 설정
    workoutExercise.setWorkoutLog(this);
  }

  public void addFeedback(Feedback feedback) {
    this.feedbacks.add(feedback);
    feedback.setWorkoutLog(this);
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null) {
      return false;
    }

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) {
      return false;
    }

    WorkoutLog that = (WorkoutLog) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}