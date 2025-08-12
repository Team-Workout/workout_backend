package com.workout.workout.domain.routine;

import com.workout.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)// JPA가 사용하는 기본 생성자성자 (빌더가 사용)
public class Routine {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  User user;

  @Column(nullable = false)
  String name;

  String description;

  @OneToMany(mappedBy = "routine", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("order ASC")
  List<RoutineExercise> routineExercises = new ArrayList<>();

  @Builder
  public Routine(User user, String name, String description) {
    this.user = user;
    this.name = name;
    this.description = description;
  }

  //== 연관관계 편의 메소드 ==//
  public void addRoutineExercise(RoutineExercise routineExercise) {
    this.routineExercises.add(routineExercise);
    routineExercise.setRoutine(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null) return false;

    Class<?> thisClass = org.hibernate.Hibernate.getClass(this);
    Class<?> thatClass = org.hibernate.Hibernate.getClass(o);
    if (thisClass != thatClass) return false;

    Routine that = (Routine) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id);
  }


}
