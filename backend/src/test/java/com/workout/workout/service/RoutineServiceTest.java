package com.workout.workout.service;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import com.workout.workout.dto.routine.RoutineCreateRequest;
import com.workout.workout.dto.routine.RoutineResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.RoutineRepository;
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
@DisplayName("RoutineService 단위 테스트")
class RoutineServiceTest {

  @InjectMocks
  private RoutineService routineService;

  @Mock
  private RoutineRepository routineRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private ExerciseRepository exerciseRepository;

  private User testUser;
  private Exercise benchPress;
  private Exercise squat;
  private RoutineCreateRequest routineCreateRequest;

  @BeforeEach
  void setUp() {
    testUser = User.builder().id(1L).name("테스트유저").build();
    benchPress = Exercise.builder().id(101L).name("벤치프레스").build();
    squat = Exercise.builder().id(102L).name("스쿼트").build();

    routineCreateRequest = new RoutineCreateRequest(
        "나의 3대 운동 루틴",
        "벤치프레스와 스쿼트를 중심으로 한 파워리프팅 루틴",
        List.of(
            new RoutineCreateRequest.RoutineExerciseDto(
                benchPress.getId(),
                1,
                List.of(
                    new RoutineCreateRequest.RoutineSetDto(1, new BigDecimal("100.5"), 5),
                    new RoutineCreateRequest.RoutineSetDto(2, new BigDecimal("100.5"), 5)
                )
            ),
            new RoutineCreateRequest.RoutineExerciseDto(
                squat.getId(),
                2,
                List.of(
                    new RoutineCreateRequest.RoutineSetDto(1, new BigDecimal("140.0"), 3)
                )
            )
        )
    );
  }

  @Nested
  @DisplayName("루틴 생성 (createRoutine)")
  class CreateRoutineTest {

    @Test
    @DisplayName("성공: 유효한 요청 시 루틴과 하위 항목들이 올바르게 생성된다")
    void createRoutine_Success() {
      // given
      given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
      given(exerciseRepository.findAllByIdIn(anyList())).willReturn(List.of(benchPress, squat));

      // [수정] .id()를 사용할 수 없으므로, ID가 설정된 객체를 반환하도록 Mocking
      Routine savedRoutine = Routine.builder().user(testUser).name(routineCreateRequest.getName()).build();
      setId(savedRoutine, 999L); // Reflection으로 ID 설정
      given(routineRepository.save(any(Routine.class))).willReturn(savedRoutine);

      // when
      Long createdRoutineId = routineService.createRoutine(routineCreateRequest, testUser.getId());

      // then
      assertThat(createdRoutineId).isEqualTo(999L); // Mocking된 ID와 일치하는지 확인

      ArgumentCaptor<Routine> routineCaptor = ArgumentCaptor.forClass(Routine.class);
      then(routineRepository).should(times(1)).save(routineCaptor.capture());
      Routine capturedRoutine = routineCaptor.getValue();

      assertThat(capturedRoutine.getName()).isEqualTo(routineCreateRequest.getName());
      assertThat(capturedRoutine.getRoutineExercises()).hasSize(2);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 사용자 ID로 요청 시 EntityNotFoundException이 발생한다")
    void createRoutine_Failure_UserNotFound() {
      // given
      given(userRepository.findById(999L)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> routineService.createRoutine(routineCreateRequest, 999L));
      then(routineRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 운동 ID가 포함된 경우 EntityNotFoundException이 발생한다")
    void createRoutine_Failure_ExerciseNotFound() {
      // given
      given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
      given(exerciseRepository.findAllByIdIn(anyList())).willReturn(List.of(benchPress));

      // when & then
      assertThrows(EntityNotFoundException.class, () -> routineService.createRoutine(routineCreateRequest, testUser.getId()));
      then(routineRepository).should(never()).save(any());
    }
  }

  @Nested
  @DisplayName("루틴 삭제 (deleteRoutine)")
  class DeleteRoutineTest {
    @Test
    @DisplayName("성공: 본인의 루틴을 삭제 요청 시 성공적으로 삭제된다")
    void deleteRoutine_Success() {
      // given
      Routine myRoutine = Routine.builder().user(testUser).name("내 루틴").build();
      setId(myRoutine, 1L); // Reflection으로 ID 설정
      given(routineRepository.findById(1L)).willReturn(Optional.of(myRoutine));

      // when
      routineService.deleteRoutine(1L, testUser.getId());

      // then
      then(routineRepository).should(times(1)).delete(myRoutine);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 루틴 ID로 삭제 요청 시 EntityNotFoundException이 발생한다")
    void deleteRoutine_Failure_RoutineNotFound() {
      // given
      given(routineRepository.findById(999L)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> routineService.deleteRoutine(999L, testUser.getId()));
      then(routineRepository).should(never()).delete(any());
    }

    @Test
    @DisplayName("실패: 다른 사용자의 루틴을 삭제 요청 시 SecurityException이 발생한다")
    void deleteRoutine_Failure_Unauthorized() {
      // given
      User anotherUser = User.builder().id(2L).build();
      Routine anotherUsersRoutine = Routine.builder().user(anotherUser).name("남의 루틴").build();
      setId(anotherUsersRoutine, 2L); // Reflection으로 ID 설정
      given(routineRepository.findById(2L)).willReturn(Optional.of(anotherUsersRoutine));

      // when & then
      assertThrows(SecurityException.class, () -> routineService.deleteRoutine(2L, testUser.getId()));
      then(routineRepository).should(never()).delete(any());
    }
  }

  @Nested
  @DisplayName("루틴 단건 조회 (findRoutineById)")
  class FindRoutineByIdTest {
    @Test
    @DisplayName("성공: 존재하는 루틴 ID로 조회 시 RoutineResponse DTO를 반환한다")
    void findRoutineById_Success() {
      // given
      Routine mockRoutine = Routine.builder().user(testUser).name("테스트 루틴").description("상세 설명").build();
      setId(mockRoutine, 1L); // Reflection으로 ID 설정

      RoutineExercise mockRoutineExercise = RoutineExercise.builder().exercise(benchPress).order(1).build();
      mockRoutine.addRoutineExercise(mockRoutineExercise);

      RoutineSet mockSet = new RoutineSet(new BigDecimal("100"), 5, 1);
      setId(mockSet, 10L); // 세트에도 ID 설정
      mockRoutineExercise.addRoutineSet(mockSet);

      given(routineRepository.findByIdWithDetails(1L)).willReturn(Optional.of(mockRoutine));

      // when
      RoutineResponse response = routineService.findRoutineById(1L);

      // then
      assertThat(response.getRoutineId()).isEqualTo(1L);
      assertThat(response.getRoutineExercises()).hasSize(1);
      assertThat(response.getRoutineExercises().get(0).getExerciseName()).isEqualTo("벤치프레스");
      assertThat(response.getRoutineExercises().get(0).getRoutineSets()).hasSize(1);
      assertThat(response.getRoutineExercises().get(0).getRoutineSets().get(0).getWorkoutSetId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("실패: 존재하지 않는 루틴 ID로 조회 시 EntityNotFoundException이 발생한다")
    void findRoutineById_Failure_RoutineNotFound() {
      // given
      given(routineRepository.findByIdWithDetails(999L)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> routineService.findRoutineById(999L));
    }
  }

  // 테스트에서 private 필드에 값을 설정하기 위한 Helper 메소드
  private void setId(Object target, Long id) {
    try {
      Field field = target.getClass().getDeclaredField("id");
      field.setAccessible(true);
      field.set(target, id);
    } catch (NoSuchFieldException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
}