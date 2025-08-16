package com.workout.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.log.Feedback;
import com.workout.workout.domain.log.WorkoutExercise;
import com.workout.workout.domain.log.WorkoutLog;
import com.workout.workout.domain.log.WorkoutSet;
import com.workout.workout.dto.log.WorkoutLogCreateRequest;
import com.workout.workout.dto.log.WorkoutLogResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.log.FeedbackRepository;
import com.workout.workout.repository.log.WorkoutExerciseRepository;
import com.workout.workout.repository.log.WorkoutLogRepository;
import com.workout.workout.repository.log.WorkoutSetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
  @Mock
  private WorkoutExerciseRepository workoutExerciseRepository;
  @Mock
  private WorkoutSetRepository workoutSetRepository;
  @Mock
  private FeedbackRepository feedbackRepository;

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
                benchPress.getId(), 1,
                List.of(new WorkoutLogCreateRequest.WorkoutSetDto(1, new BigDecimal("100"), 5, "자극이 좋았음"))
            )
        )
    );
  }

  @Nested
  @DisplayName("운동일지 생성 (createWorkoutLog)")
  class CreateWorkoutLogTest {

    @Test
    @DisplayName("성공: 유효한 요청 시 운동일지와 하위 항목들이 saveAll을 통해 올바르게 저장된다")
    void createWorkoutLog_Success() {
      // given
      given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
      given(exerciseRepository.findAllByIdIn(anyList())).willReturn(List.of(benchPress));
      WorkoutLog savedLog = new WorkoutLog(testUser, createRequest.workoutDate());
      setId(savedLog, 999L);
      given(workoutLogRepository.save(any(WorkoutLog.class))).willReturn(savedLog);

      // when
      workoutLogService.createWorkoutLog(createRequest, testUser.getId());

      // then
      then(workoutLogRepository).should(times(1)).save(any(WorkoutLog.class));

      ArgumentCaptor<List<WorkoutExercise>> exercisesCaptor = ArgumentCaptor.forClass(List.class);
      then(workoutExerciseRepository).should(times(1)).saveAll(exercisesCaptor.capture());
      assertThat(exercisesCaptor.getValue()).hasSize(1);

      ArgumentCaptor<List<WorkoutSet>> setsCaptor = ArgumentCaptor.forClass(List.class);
      then(workoutSetRepository).should(times(1)).saveAll(setsCaptor.capture());
      assertThat(setsCaptor.getValue()).hasSize(1);

      ArgumentCaptor<List<Feedback>> feedbacksCaptor = ArgumentCaptor.forClass(List.class);
      then(feedbackRepository).should(times(1)).saveAll(feedbacksCaptor.capture());
      assertThat(feedbacksCaptor.getValue()).hasSize(2); // 로그 피드백 1개, 세트 피드백 1개
    }
  }

  @Nested
  @DisplayName("운동일지 삭제 (deleteWorkoutLog)")
  class DeleteWorkoutLogTest {

    @Test
    @DisplayName("성공: 본인의 운동일지를 삭제 요청 시 하위 항목부터 순차적으로 삭제된다")
    void deleteWorkoutLog_Success() {
      // given
      Long logId = 1L;
      Long userId = 1L;
      List<Long> exerciseIds = List.of(10L, 11L);
      List<Long> setIds = List.of(101L, 102L);

      given(workoutLogRepository.existsByIdAndUserId(logId, userId)).willReturn(true);
      given(workoutExerciseRepository.findIdsByWorkoutLogId(logId)).willReturn(exerciseIds);
      given(workoutSetRepository.findIdsByWorkoutExerciseIdIn(exerciseIds)).willReturn(setIds);

      // when
      workoutLogService.deleteWorkoutLog(logId, userId);

      // then
      // 삭제 순서 검증
      then(feedbackRepository).should(times(1)).deleteAllByWorkoutSetIdIn(setIds);
      then(feedbackRepository).should(times(1)).deleteAllByWorkoutExerciseIdIn(exerciseIds);
      then(feedbackRepository).should(times(1)).deleteAllByWorkoutLogId(logId);
      then(workoutSetRepository).should(times(1)).deleteAllByWorkoutExerciseIdIn(exerciseIds);
      then(workoutExerciseRepository).should(times(1)).deleteAllByWorkoutLogId(logId);
      then(workoutLogRepository).should(times(1)).deleteById(logId);
    }
  }

  @Nested
  @DisplayName("운동일지 단건 조회 (findWorkoutLogById)")
  class FindWorkoutLogByIdTest {
    @Test
    @DisplayName("성공: 존재하는 ID로 조회 시 각 Repository를 호출하여 DTO를 조립해 반환한다")
    void findWorkoutLogById_Success() {
      // given
      Long logId = 1L;
      WorkoutLog mockLog = new WorkoutLog(testUser, LocalDate.now());
      setId(mockLog, logId);
      WorkoutExercise mockExercise = WorkoutExercise.builder().workoutLog(mockLog).exercise(benchPress).order(1).build();
      setId(mockExercise, 10L);
      WorkoutSet mockSet = WorkoutSet.builder().workoutExercise(mockExercise).weight(BigDecimal.TEN).reps(10).order(1).build();
      setId(mockSet, 101L);
      Feedback mockFeedback = Feedback.builder().author(testUser).content("좋아요").workoutLog(mockLog).build();
      setId(mockFeedback, 1001L);

      given(workoutLogRepository.findById(logId)).willReturn(Optional.of(mockLog));
      given(workoutExerciseRepository.findAllByWorkoutLogIdOrderByOrderAsc(logId)).willReturn(List.of(mockExercise));
      given(workoutSetRepository.findAllByWorkoutExerciseIdInOrderByOrderAsc(List.of(10L))).willReturn(List.of(mockSet));
      given(feedbackRepository.findByWorkoutElements(logId, List.of(10L), List.of(101L))).willReturn(List.of(mockFeedback));

      // when
      WorkoutLogResponse response = workoutLogService.findWorkoutLogById(logId);

      // then
      assertThat(response.workoutLogId()).isEqualTo(logId);
      assertThat(response.workoutExercises()).hasSize(1);
      assertThat(response.workoutExercises().get(0).workoutSets()).hasSize(1);
      assertThat(response.feedbacks()).hasSize(1);
      assertThat(response.feedbacks().iterator().next().content()).isEqualTo("좋아요");
    }
  }

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