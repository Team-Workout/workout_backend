package com.workout.workout.domain.log;

import com.workout.workout.domain.exercise.Exercise;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Setter
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_log_id")
  private WorkoutLog workoutLog;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "exercise_id")
  private Exercise exercise; // 어떤 운동을 했는지 (마스터 데이터 참조)

  private int setNumber;

  private BigDecimal weight;

  private int reps;

  // WorkoutSet이 삭제되면 하위 피드백들도 모두 함께 삭제됩니다.
  @OneToMany(mappedBy = "workoutSet", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkoutFeedback> feedbacks = new ArrayList<>();

  @Builder
  public WorkoutSet(Exercise exercise, int setNumber, BigDecimal weight, int reps) {
    this.exercise = exercise;
    this.setNumber = setNumber;
    this.weight = weight;
    this.reps = reps;
  }

  //== 연관관계 편의 메소드 ==//
  public void addFeedback(WorkoutFeedback feedback) {
    this.feedbacks.add(feedback);
    feedback.setWorkoutSet(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) return false;

    WorkoutSet that = (WorkoutSet) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }
}