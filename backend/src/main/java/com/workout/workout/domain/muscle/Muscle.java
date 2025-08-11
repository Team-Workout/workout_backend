package com.workout.workout.domain.muscle;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Muscle {
  // 데이터베이스의 target_muscle 테이블과 1:1로 매핑
  CHEST("가슴"),
  BACK("등"),
  SHOULDERS("어깨"),
  BICEPS("이두근"),
  TRICEPS("삼두근"),
  FOREARM("전완근"),
  ABS("복근"),
  GLUTES("둔근"),
  QUADS("대퇴사두근"),
  HAMSTRINGS("햄스트링"),
  CALVES("종아리");

  private final String koreanName;
}