/*
package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.workOut.WorkoutLogCreateRequest;
import com.workout.workout.dto.workOut.WorkoutLogResponse;
import com.workout.workout.dto.workOut.WorkoutSetDto;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.WorkoutLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutLogService 단위 테스트")
class WorkoutLogServiceTest {

  @Mock
  private WorkoutLogRepository workoutLogRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ExerciseRepository exerciseRepository;

  @InjectMocks
  private WorkoutLogService workoutLogService;

  @Nested
  @DisplayName("운동일지 생성 테스트")
  class CreateWorkoutLogTest {

    @Test
    @DisplayName("성공: 올바른 데이터로 운동일지 생성에 성공한다")
    void createWorkoutLog_success() {
      // given (주어진 상황)
      Long userId = 1L;
      Long benchPressId = 10L;
      Long squatId = 20L;

      // 1. 요청 DTO 준비
      WorkoutSetDto setDto1 = new WorkoutSetDto(benchPressId, new BigDecimal("100"), 10);
      WorkoutSetDto setDto2 = new WorkoutSetDto(squatId, new BigDecimal("120"), 8);
      WorkoutLogCreateRequest request = new WorkoutLogCreateRequest(LocalDate.now(), "오늘 운동 완료!", List.of(setDto1, setDto2));

      // 2. Mock 객체들의 행동 정의
      User mockUser = User.builder().id(userId).name("테스트유저").build();
      Exercise mockBenchPress = Exercise.builder().id(benchPressId).name("벤치프레스").build();
      Exercise mockSquat = Exercise.builder().id(squatId).name("스쿼트").build();

      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(exerciseRepository.findById(benchPressId)).willReturn(Optional.of(mockBenchPress));
      given(exerciseRepository.findById(squatId)).willReturn(Optional.of(mockSquat));
      // save 메소드가 호출되면, 전달된 workoutLog 객체를 그대로 반환하도록 설정
      given(workoutLogRepository.save(any(WorkoutLog.class))).willAnswer(invocation -> {
        WorkoutLog log = invocation.getArgument(0);
        // 실제 DB처럼 ID가 부여된 것처럼 흉내 낼 수 있지만, 여기서는 객체 반환만으로 충분
        return log;
      });

      // when (무엇을 할 때)
      workoutLogService.createWorkoutLog(request, userId);

      // then (결과 확인)
      // 1. workoutLogRepository의 save 메소드가 1번 호출되었는지 검증
      ArgumentCaptor<WorkoutLog> captor = ArgumentCaptor.forClass(WorkoutLog.class);
      then(workoutLogRepository).should(times(1)).save(captor.capture());

      // 2. 저장된 WorkoutLog 객체의 내용 검증
      WorkoutLog capturedLog = captor.getValue();
      assertThat(capturedLog.getUserMemo()).isEqualTo("오늘 운동 완료!");
      assertThat(capturedLog.getUser().getId()).isEqualTo(userId);
      assertThat(capturedLog.getWorkoutSets()).hasSize(2);
      assertThat(capturedLog.getWorkoutSets().get(0).getExercise().getName()).isEqualTo("벤치프레스");
      assertThat(capturedLog.getWorkoutSets().get(1).getExercise().getName()).isEqualTo("스쿼트");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자로 일지를 생성하려고 하면 예외가 발생한다")
    void createWorkoutLog_failure_userNotFound() {
      // given
      Long nonExistentUserId = 999L;
      WorkoutLogCreateRequest request = new WorkoutLogCreateRequest(LocalDate.now(), "메모", List.of());
      given(userRepository.findById(nonExistentUserId)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class,
          () -> workoutLogService.createWorkoutLog(request, nonExistentUserId));

      // save 메소드가 절대 호출되지 않았는지 검증
      then(workoutLogRepository).should(never()).save(any(WorkoutLog.class));
    }

    @Test
    @DisplayName("실패: 존재하지 않는 운동으로 일지를 생성하려고 하면 예외가 발생한다")
    void createWorkoutLog_failure_exerciseNotFound() {
      // given
      Long userId = 1L;
      Long existentExerciseId = 10L;
      Long nonExistentExerciseId = 999L; // 존재하지 않는 운동 ID
      WorkoutSetDto setDto1 = new WorkoutSetDto(existentExerciseId, new BigDecimal("100"), 10);
      WorkoutSetDto setDto2 = new WorkoutSetDto(nonExistentExerciseId, new BigDecimal("120"), 8);
      WorkoutLogCreateRequest request = new WorkoutLogCreateRequest(LocalDate.now(), "메모", List.of(setDto1, setDto2));

      User mockUser = User.builder().id(userId).build();
      Exercise mockExercise = Exercise.builder().id(existentExerciseId).build();

      given(userRepository.findById(userId)).willReturn(Optional.of(mockUser));
      given(exerciseRepository.findById(existentExerciseId)).willReturn(Optional.of(mockExercise));
      given(exerciseRepository.findById(nonExistentExerciseId)).willReturn(Optional.empty()); // 이 운동은 찾을 수 없음

      // when & then
      assertThrows(EntityNotFoundException.class,
          () -> workoutLogService.createWorkoutLog(request, userId));

      then(workoutLogRepository).should(never()).save(any(WorkoutLog.class));
    }
  }

  @Nested
  @DisplayName("운동일지 조회 테스트")
  class FindWorkoutLogTest {

    @Test
    @DisplayName("성공: ID로 운동일지를 성공적으로 조회한다")
    void findWorkoutLogById_success() {
      // given
      Long logId = 1L;
      User mockUser = User.builder().id(1L).name("테스트유저").build();
      Exercise mockExercise = Exercise.builder().id(10L).name("벤치프레스").build();
      WorkoutLog mockWorkoutLog = WorkoutLog.builder().user(mockUser).workoutDate(LocalDate.now()).build();
      mockWorkoutLog.addWorkoutSet(
          WorkoutSet.builder().exercise(mockExercise).setNumber(1).reps(10).weight(new BigDecimal("100")).build());

      given(workoutLogRepository.findByIdWithSets(logId)).willReturn(Optional.of(mockWorkoutLog));

      // when
      WorkoutLogResponse response = workoutLogService.findWorkoutLogById(logId);

      // then
      assertThat(response.getUserMemo()).isEqualTo(mockWorkoutLog.getUserMemo());
      assertThat(response.getWorkoutSets()).hasSize(1);
      assertThat(response.getWorkoutSets().get(0).getExerciseName()).isEqualTo("벤치프레스");
      then(workoutLogRepository).should(times(1)).findByIdWithSets(logId);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회하면 예외가 발생한다")
    void findWorkoutLogById_failure_logNotFound() {
      // given
      Long nonExistentLogId = 999L;
      given(workoutLogRepository.findByIdWithSets(nonExistentLogId)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class,
          () -> workoutLogService.findWorkoutLogById(nonExistentLogId));
    }
  }
}*/
