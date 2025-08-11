package com.workout.workout.domain.log;

import com.workout.global.Auditable;
import com.workout.user.domain.User;
import jakarta.persistence.*;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class WorkoutLog implements Auditable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "workout_date", nullable = false) // DB 컬럼명도 역할에 맞게 변경
  private LocalDate workoutDate;

  private String userMemo; // 운동일지 전체에 대한 메모

  // WorkoutLog의 생명주기에 WorkoutSet이 완전히 종속됩니다.
  @OneToMany(mappedBy = "workoutLog", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkoutSet> workoutSets = new ArrayList<>();

  @Builder
  public WorkoutLog(User user, LocalDate workoutDate, String userMemo) {
    this.user = user;
    this.workoutDate = workoutDate;
    this.userMemo = userMemo;
  }

  //== 연관관계 편의 메소드 ==//
  public void addWorkoutSet(WorkoutSet workoutSet) {
    this.workoutSets.add(workoutSet);
    workoutSet.setWorkoutLog(this); // [수정] 이 부분의 주석을 해제해야 합니다.
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