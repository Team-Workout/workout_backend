package com.workout.workout.domain.log;

import com.workout.global.AuditableEntity;
import com.workout.user.domain.User;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class WorkoutLog extends AuditableEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @Column(name = "workout_date", nullable = false)
  private LocalDate workoutDate;

  @OneToMany(mappedBy = "workoutLog", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("logOrder ASC")
  private List<WorkoutExercise> workoutExercises = new ArrayList<>();

  @OneToMany(mappedBy = "workoutLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private Set<Feedback> feedbacks = new HashSet<>();

  // [수정] 불필요한 userMemo 파라미터 제거
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
    if (this == o) return true;
    if (o == null) return false;

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) return false;

    WorkoutLog that = (WorkoutLog) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}