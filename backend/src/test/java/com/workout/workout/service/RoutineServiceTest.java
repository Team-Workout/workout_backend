package com.workout.workout.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import com.workout.user.domain.User;
import com.workout.user.repository.UserRepository;
import com.workout.workout.domain.exercise.Exercise;
import com.workout.workout.domain.routine.Routine;
import com.workout.workout.domain.routine.RoutineExercise;
import com.workout.workout.domain.routine.RoutineSet;
import com.workout.workout.dto.routine.RoutineCreateRequest;
import com.workout.workout.dto.routine.RoutineResponse;
import com.workout.workout.repository.ExerciseRepository;
import com.workout.workout.repository.routine.RoutineExerciseRepository;
import com.workout.workout.repository.routine.RoutineRepository;
import com.workout.workout.repository.routine.RoutineSetRepository;
import jakarta.persistence.EntityNotFoundException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
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
  @Mock
  private RoutineExerciseRepository routineExerciseRepository;
  @Mock
  private RoutineSetRepository routineSetRepository;

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
    @DisplayName("성공: 유효한 요청 시 루틴과 하위 항목들이 saveAll을 통해 올바르게 저장된다")
    void createRoutine_Success() {
      // given
      given(userRepository.findById(testUser.getId())).willReturn(Optional.of(testUser));
      given(exerciseRepository.findAllByIdIn(anyList())).willReturn(List.of(benchPress, squat));

      Routine savedRoutine = Routine.builder().user(testUser).name(routineCreateRequest.name()).build();
      setId(savedRoutine, 999L);
      given(routineRepository.save(any(Routine.class))).willReturn(savedRoutine);

      // when
      Long createdRoutineId = routineService.createRoutine(routineCreateRequest, testUser.getId());

      // then
      assertThat(createdRoutineId).isEqualTo(999L);

      // 1. routineRepository.save가 호출되었는지 검증
      then(routineRepository).should(times(1)).save(any(Routine.class));

      // 2. routineExerciseRepository.saveAll이 호출되었는지, 내용은 무엇인지 검증
      ArgumentCaptor<List<RoutineExercise>> exercisesCaptor = ArgumentCaptor.forClass(List.class);
      then(routineExerciseRepository).should(times(1)).saveAll(exercisesCaptor.capture());
      List<RoutineExercise> capturedExercises = exercisesCaptor.getValue();
      assertThat(capturedExercises).hasSize(2);
      assertThat(capturedExercises.get(0).getExercise().getName()).isEqualTo("벤치프레스");
      assertThat(capturedExercises.get(0).getRoutine().getId()).isEqualTo(999L); // 부모 Routine과 연결되었는지 확인

      // 3. routineSetRepository.saveAll이 호출되었는지, 내용은 무엇인지 검증
      ArgumentCaptor<List<RoutineSet>> setsCaptor = ArgumentCaptor.forClass(List.class);
      then(routineSetRepository).should(times(1)).saveAll(setsCaptor.capture());
      List<RoutineSet> capturedSets = setsCaptor.getValue();
      assertThat(capturedSets).hasSize(3); // (2+1)
      assertThat(capturedSets.get(0).getWeight()).isEqualTo(new BigDecimal("100.5"));
      assertThat(capturedSets.get(0).getRoutineExercise()).isEqualTo(capturedExercises.get(0)); // 부모 Exercise와 연결되었는지 확인
    }
  }

  @Nested
  @DisplayName("루틴 삭제 (deleteRoutine)")
  class DeleteRoutineTest {

    @Test
    @DisplayName("성공: 본인의 루틴을 삭제 요청 시 하위 항목부터 순차적으로 삭제된다")
    void deleteRoutine_Success() {
      // given
      Routine myRoutine = Routine.builder().user(testUser).name("내 루틴").build();
      setId(myRoutine, 1L);
      given(routineRepository.findById(1L)).willReturn(Optional.of(myRoutine));

      RoutineExercise re1 = RoutineExercise.builder().build();
      setId(re1, 10L);
      RoutineExercise re2 = RoutineExercise.builder().build();
      setId(re2, 11L);
      List<RoutineExercise> exercises = List.of(re1, re2);
      List<Long> exerciseIds = List.of(10L, 11L);

      given(routineExerciseRepository.findAllByRoutineIdOrderByOrderAsc(1L)).willReturn(exercises);

      // when
      routineService.deleteRoutine(1L, testUser.getId());

      // then
      // 삭제 순서(손자 -> 자식 -> 부모)와 호출 인자를 정확히 검증
      then(routineSetRepository).should(times(1)).deleteAllByRoutineExerciseIdIn(exerciseIds);
      then(routineExerciseRepository).should(times(1)).deleteAllByRoutineId(1L);
      then(routineRepository).should(times(1)).delete(myRoutine);
    }
  }

  @Nested
  @DisplayName("루틴 단건 조회 (findRoutineById)")
  class FindRoutineByIdTest {

    @Test
    @DisplayName("성공: 존재하는 루틴 ID로 조회 시 각 Repository를 호출하여 DTO를 조립해 반환한다")
    void findRoutineById_Success() {
      // given --- Mocking 방식 변경 ---
      // 각 계층별로 데이터를 분리하여 Mocking
      Routine mockRoutine = Routine.builder().user(testUser).name("테스트 루틴").description("상세 설명").build();
      setId(mockRoutine, 1L);

      RoutineExercise mockRe1 = RoutineExercise.builder().routine(mockRoutine).exercise(benchPress).order(1).build();
      setId(mockRe1, 10L);
      RoutineExercise mockRe2 = RoutineExercise.builder().routine(mockRoutine).exercise(squat).order(2).build();
      setId(mockRe2, 11L);

      RoutineSet mockSet1 = RoutineSet.builder().routineExercise(mockRe1).weight(new BigDecimal("100")).reps(5).order(1).build();
      setId(mockSet1, 101L);

      given(routineRepository.findById(1L)).willReturn(Optional.of(mockRoutine));
      given(routineExerciseRepository.findAllByRoutineIdOrderByOrderAsc(1L)).willReturn(List.of(mockRe1, mockRe2));
      given(routineSetRepository.findAllByRoutineExerciseIdInOrderByOrderAsc(List.of(10L, 11L))).willReturn(List.of(mockSet1));

      // when
      RoutineResponse response = routineService.findRoutineById(1L);

      // then
      // 최종적으로 조립된 DTO의 내용물을 검증
      assertThat(response.routineId()).isEqualTo(1L);
      assertThat(response.routineName()).isEqualTo("테스트 루틴");
      assertThat(response.routineExercises()).hasSize(2);
      assertThat(response.routineExercises().get(0).exerciseName()).isEqualTo("벤치프레스");
      assertThat(response.routineExercises().get(0).routineSets()).hasSize(1);
      assertThat(response.routineExercises().get(0).routineSets().get(0).workoutSetId()).isEqualTo(101L);
      assertThat(response.routineExercises().get(1).routineSets()).isEmpty(); // 스쿼트에는 세트가 없음
    }

    @Test
    @DisplayName("실패: 존재하지 않는 루틴 ID로 조회 시 EntityNotFoundException이 발생한다")
    void findRoutineById_Failure_RoutineNotFound() {
      // given
      given(routineRepository.findById(999L)).willReturn(Optional.empty());

      // when & then
      assertThrows(EntityNotFoundException.class, () -> routineService.findRoutineById(999L));
      // 다른 레포지토리는 호출되지 않아야 함
      then(routineExerciseRepository).should(never()).findAllByRoutineIdOrderByOrderAsc(any());
    }
  }

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