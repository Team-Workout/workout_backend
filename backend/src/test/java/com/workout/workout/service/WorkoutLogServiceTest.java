package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.WorkoutLogRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
@DisplayName("WorkoutLogService 단위 테스트")
class WorkoutLogServiceTest {

  @InjectMocks
  private WorkoutLogService workoutLogService;

  @Mock
  private WorkoutLogRepository workoutLogRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ExerciseRepository exerciseRepository;

  // 테스트 픽스처
  private User testUser;
  private Exercise benchPress;
  private Exercise squat;
  private WorkoutLogCreateRequest createRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).name("테스트유저").build();
    benchPress = Exercise.builder().id(101L).name("벤치프레스").build();
    squat = Exercise.builder().id(102L).name("스쿼트").build();

    createRequest = new WorkoutLogCreateRequest(
        LocalDate.of(2025, 8, 13),
        "오늘 운동 만족스러웠다.",
        List.of(
            new WorkoutLogCreateRequest.WorkoutExerciseDto(
                benchPress.getId(),
                1,
                List.of(
                    new WorkoutLogCreateRequest.WorkoutSetDto(1, new BigDecimal("100"), 5, "자극이 좋았음"),
                    new WorkoutLogCreateRequest.WorkoutSetDto(2, new BigDecimal("100"), 5, null) // 피드백 없는 세트
                )
            ),
            new WorkoutLogCreateRequest.WorkoutExerciseDto(
                squat.getId(),
                2,
                List.of(
                    new WorkoutLogCreateRequest.WorkoutSetDto(1, new BigDecimal("140"), 3, "조금 무거웠다.")
                )
            )
        )
    );
  }

  @Nested
  @DisplayName("운동일지 생성 (createWorkoutLog)")
  class CreateWorkoutLogTest {

    @Test
    @DisplayName("성공: 유효한 요청 시 운동일지와 하위 항목(피드백 포함)들이 올바르게 생성된다")
    void createWorkoutLog_Success() {
      // given
      given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
      given(exerciseRepository.findAllByIdIn(anyList())).willReturn(List.of(benchPress, squat));

      // save 시 ID가 부여된 객체를 반환하도록 설정
      WorkoutLog savedLog = new WorkoutLog(testUser, createRequest.workoutDate());
      setId(savedLog, 999L);
      given(workoutLogRepository.save(any(WorkoutLog.class))).willReturn(savedLog);

      // when
      Long createdLogId = workoutLogService.createWorkoutLog(createRequest, testUser.getId());

      // then
      assertThat(createdLogId).isEqualTo(999L);

      ArgumentCaptor<WorkoutLog> logCaptor = ArgumentCaptor.forClass(WorkoutLog.class);
      then(workoutLogRepository).should(times(1)).save(logCaptor.capture());
      WorkoutLog capturedLog = logCaptor.getValue();

      // 최상위 로그 정보 검증
      assertThat(capturedLog.getUser()).isEqualTo(testUser);
      assertThat(capturedLog.getWorkoutDate()).isEqualTo(createRequest.workoutDate());
      assertThat(capturedLog.getFeedbacks()).hasSize(1);
      assertThat(capturedLog.getFeedbacks().iterator().next().getContent()).isEqualTo("오늘 운동 만족스러웠다.");

      // 운동 그룹 정보 검증
      assertThat(capturedLog.getWorkoutExercises()).hasSize(2);
      WorkoutExercise capturedBenchPress = capturedLog.getWorkoutExercises().get(0);
      assertThat(capturedBenchPress.getExercise()).isEqualTo(benchPress);

      // 운동 세트 및 세트 피드백 정보 검증
      assertThat(capturedBenchPress.getWorkoutSets()).hasSize(2);
      WorkoutSet firstSet = capturedBenchPress.getWorkoutSets().get(0);
      assertThat(firstSet.getFeedbacks()).hasSize(1);
      assertThat(firstSet.getFeedbacks().iterator().next().getContent()).isEqualTo("자극이 좋았음");
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자 ID로 요청 시 EntityNotFoundException이 발생한다")
    void createWorkoutLog_Failure_UserNotFound() {
      // given
      given(userRepository.findById(999L)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> workoutLogService.createWorkoutLog(createRequest, 999L));
      then(workoutLogRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 운동 ID가 포함된 경우 EntityNotFoundException이 발생한다")
    void createWorkoutLog_Failure_ExerciseNotFound() {
      // given
      given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
      given(exerciseRepository.findAllByIdIn(anyList())).willReturn(List.of(benchPress)); // 스쿼트 정보 누락

      // when & then
      assertThrows(EntityNotFoundException.class, () -> workoutLogService.createWorkoutLog(createRequest, testUser.getId()));
      then(workoutLogRepository).should(never()).save(any());
    }
  }

  @Nested
  @DisplayName("운동일지 삭제 (deleteWorkoutLog)")
  class DeleteWorkoutLogTest {

    @Test
    @DisplayName("성공: 본인의 운동일지를 삭제 요청 시 성공적으로 삭제된다")
    void deleteWorkoutLog_Success() {
      // given
      WorkoutLog myLog = new WorkoutLog(testUser, LocalDate.now());
      setId(myLog, 1L);
      given(workoutLogRepository.findById(1L)).willReturn(Optional.of(myLog));

      // when
      workoutLogService.deleteWorkoutLog(1L, testUser.getId());

      // then
      then(workoutLogRepository).should(times(1)).delete(myLog);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 삭제 요청 시 EntityNotFoundException이 발생한다")
    void deleteWorkoutLog_Failure_LogNotFound() {
      // given
      given(workoutLogRepository.findById(999L)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> workoutLogService.deleteWorkoutLog(999L, testUser.getId()));
      then(workoutLogRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("실패: 다른 사용자의 운동일지를 삭제 요청 시 SecurityException이 발생한다")
    void deleteWorkoutLog_Failure_Unauthorized() {
      // given
      User anotherUser = User.builder().id(2L).build();
      WorkoutLog anotherUsersLog = new WorkoutLog(anotherUser, LocalDate.now());
      setId(anotherUsersLog, 2L);
      given(workoutLogRepository.findById(2L)).willReturn(Optional.of(anotherUsersLog));

      // when & then
      assertThrows(SecurityException.class, () -> workoutLogService.deleteWorkoutLog(2L, testUser.getId()));
      then(workoutLogRepository).should(never()).delete(any());
    }
  }

  @Nested
  @DisplayName("운동일지 단건 조회 (findWorkoutLogById)")
  class FindWorkoutLogByIdTest {

    @Test
    @DisplayName("성공: 존재하는 ID로 조회 시 WorkoutLogResponse DTO를 반환한다")
    void findWorkoutLogById_Success() {
      // given
      WorkoutLog mockLog = new WorkoutLog(testUser, LocalDate.now());
      setId(mockLog, 1L);
      // ... 복잡한 객체 그래프 생성 (필요 시) ...

      given(workoutLogRepository.findByIdWithDetails(1L)).willReturn(Optional.of(mockLog));

      // when
      WorkoutLogResponse response = workoutLogService.findWorkoutLogById(1L);

      // then
      assertThat(response).isNotNull();
      assertThat(response.workoutLogId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 ID로 조회 시 EntityNotFoundException이 발생한다")
    void findWorkoutLogById_Failure_LogNotFound() {
      // given
      given(workoutLogRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> workoutLogService.findWorkoutLogById(999L));
    }
  }

  // 테스트에서 private id 필드에 값을 설정하기 위한 Helper 메소드
  private void setId(Object target, Long id) {
    try {
      Field idField = target.getClass().getDeclaredField("id");
      idField.setAccessible(true);
      idField.set(target, id);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException("테스트 객체 ID 설정 중 오류 발생", e);
    }
  }
}