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
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import jakarta.persistence.OrderBy;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WorkoutSet {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "workout_exercise_id")
  private WorkoutExercise workoutExercise;

  private int setNumber;

  private BigDecimal weight;

  private int reps;

  @OneToMany(mappedBy = "workoutSet", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("createdAt ASC")
  private Set<Feedback> feedbacks = new HashSet<>();


  @Builder
  public WorkoutSet(int setNumber, BigDecimal weight, int reps) {
    this.setNumber = setNumber;
    this.weight = weight;
    this.reps = reps;
  }

  //== 연관관계 편의 메소드 ==//
  protected void setWorkoutExercise(WorkoutExercise workoutExercise){
    this.workoutExercise = workoutExercise;
  }

  public void addFeedback(Feedback feedback) {
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