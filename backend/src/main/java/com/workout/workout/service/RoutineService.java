package com.workout.workout.service;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.WorkoutErrorCode;
import com.workout.member.domain.Member;
import com.workout.member.service.MemberService;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.service.TrainerService;
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
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoutineService {

  private final RoutineRepository routineRepository;
  private final ExerciseRepository exerciseRepository;
  private final RoutineExerciseRepository routineExerciseRepository;
  private final RoutineSetRepository routineSetRepository;
  private final MemberService memberService;
  private final PTContractService ptContractService;
  private final TrainerService trainerService;


  public RoutineService(
      RoutineRepository routineRepository, TrainerService trainerService,
      ExerciseRepository exerciseRepository, RoutineExerciseRepository routineExerciseRepository,
      RoutineSetRepository routineSetRepository, MemberService memberService,
      PTContractService ptContractService) {
    this.routineRepository = routineRepository;
    this.exerciseRepository = exerciseRepository;
    this.routineExerciseRepository = routineExerciseRepository;
    this.routineSetRepository = routineSetRepository;
    this.memberService = memberService;
    this.ptContractService = ptContractService;
    this.trainerService = trainerService;
  }

  @Transactional
  public Long createRoutine(@Valid RoutineCreateRequest request, Long userId) {
    Member member = memberService.findById(userId);

    Routine routine = request.toEntity(member);

    Routine savedRoutine = routineRepository.save(routine);

    List<Long> exerciseIds = request.routineExercises().stream()
        .map(RoutineCreateRequest.RoutineExerciseDto::exerciseId)
        .distinct()
        .collect(Collectors.toList());

    Map<Long, Exercise> exerciseMap = exerciseRepository.findAllByIdIn(exerciseIds).stream()
        .collect(Collectors.toMap(Exercise::getId, e -> e));

    List<RoutineExercise> routineExercisesToSave = new ArrayList<>();
    List<RoutineSet> routineSetsToSave = new ArrayList<>();

    request.routineExercises().forEach(exerciseDto -> {
      Exercise exercise = exerciseMap.get(exerciseDto.exerciseId());
      if (exercise == null) {
        throw new RestApiException(WorkoutErrorCode.NOT_FOUND_EXERCISE);
      }

      RoutineExercise routineExercise = exerciseDto.toEntity(savedRoutine, exercise);
      routineExercisesToSave.add(routineExercise);

      exerciseDto.routineSets().forEach(setDto -> {
        RoutineSet routineSet = setDto.toEntity(routineExercise);
        routineSetsToSave.add(routineSet);
      });
    });

    routineExerciseRepository.saveAll(routineExercisesToSave);
    routineSetRepository.saveAll(routineSetsToSave);

    return savedRoutine.getId();
  }

  @Transactional
  public void deleteRoutine(Long id, Long userId) {
    Routine routine = routineRepository.findById(id)
        .orElseThrow(() -> new RestApiException(WorkoutErrorCode.NOT_FOUND_ROUTINE));

    if (!routine.getMember().getId().equals(userId)) {
      throw new RestApiException(WorkoutErrorCode.NOT_ALLOWED_ACCESS);
    }

    // 1. 삭제할 자식(RoutineExercise)들의 ID 목록만 조회합니다. (Set 삭제에 필요)
    List<Long> routineExerciseIds = routineExerciseRepository.findAllByRoutineIdOrderByOrderAsc(id)
        .stream()
        .map(RoutineExercise::getId)
        .toList();

    // 2. 자손(RoutineSet)들을 SELECT 없이 바로 삭제합니다.
    if (!routineExerciseIds.isEmpty()) {
      routineSetRepository.deleteAllByRoutineExerciseIdIn(routineExerciseIds);
    }

    // 3. 자식(RoutineExercise)들을 SELECT 없이 바로 삭제합니다.
    routineExerciseRepository.deleteAllByRoutineId(id);

    // 4. 부모(Routine)를 삭제합니다.
    routineRepository.delete(routine);
  }

  public RoutineResponse findRoutineById(Long routineId) {
    // 부모(Routine) 조회
    Routine routine = routineRepository.findById(routineId)
        .orElseThrow(() -> new RestApiException(WorkoutErrorCode.NOT_FOUND_ROUTINE));

    // 자식(RoutineExercise) 목록 조회
    List<RoutineExercise> routineExercises = routineExerciseRepository.findAllByRoutineIdOrderByOrderAsc(
        routineId);

    // 자손(RoutineSet) 목록을 한 번의 쿼리로 조회 후 맵으로 그룹핑
    Map<Long, List<RoutineSet>> routineSetsMap;
    if (routineExercises.isEmpty()) {
      routineSetsMap = new HashMap<>();
    } else {
      List<Long> routineExerciseIds = routineExercises.stream()
          .map(RoutineExercise::getId)
          .toList();
      List<RoutineSet> routineSets = routineSetRepository.findAllByRoutineExerciseIdInOrderByOrderAsc(
          routineExerciseIds);
      routineSetsMap = routineSets.stream()
          .collect(Collectors.groupingBy(rs -> rs.getRoutineExercise().getId()));
    }

    // 서비스 계층에서 DTO로 조립하여 반환
    return RoutineResponse.from(routine, routineExercises, routineSetsMap);
  }

  public List<RoutineResponse> findAllRoutinesByUserId(Long userId) {
    // 1. 사용자의 모든 루틴 조회
    List<Routine> routines = routineRepository.findAllRoutinesByMemberId(userId);

    if (routines.isEmpty()) {
      return Collections.emptyList();
    }

    // 2. 루틴 ID 목록 추출
    List<Long> routineIds = routines.stream()
        .map(Routine::getId)
        .toList();

    // 3. 루틴에 속한 모든 RoutineExercise를 한 번의 쿼리로 조회 (N+1 방지)
    // ※ 참고: RoutineExerciseRepository에 findAllByRoutineIdInOrderByOrderAsc 메소드 추가 필요
    List<RoutineExercise> allRoutineExercises = routineExerciseRepository.findAllByRoutineIdInOrderByOrderAsc(
        routineIds);

    Map<Long, List<RoutineSet>> routineSetsMap;
    if (allRoutineExercises.isEmpty()) {
      routineSetsMap = Collections.emptyMap();
    } else {
      // 4. RoutineExercise에 속한 모든 RoutineSet을 한 번의 쿼리로 조회 (N+1 방지)
      List<Long> allRoutineExerciseIds = allRoutineExercises.stream()
          .map(RoutineExercise::getId)
          .toList();
      List<RoutineSet> allRoutineSets = routineSetRepository.findAllByRoutineExerciseIdInOrderByOrderAsc(
          allRoutineExerciseIds);
      // 5. RoutineSet들을 RoutineExercise ID를 기준으로 그룹핑
      routineSetsMap = allRoutineSets.stream()
          .collect(Collectors.groupingBy(rs -> rs.getRoutineExercise().getId()));
    }

    // 6. RoutineExercise들을 루틴 ID를 기준으로 그룹핑
    Map<Long, List<RoutineExercise>> routineExercisesMap = allRoutineExercises.stream()
        .collect(Collectors.groupingBy(re -> re.getRoutine().getId()));

    // 7. 조회된 데이터를 RoutineResponse DTO 리스트로 조립
    return routines.stream()
        .map(routine -> {
          List<RoutineExercise> exercisesForRoutine = routineExercisesMap.getOrDefault(
              routine.getId(), Collections.emptyList());
          return RoutineResponse.from(routine, exercisesForRoutine, routineSetsMap);
        })
        .collect(Collectors.toList());
  }

  @Transactional
  public Long createRoutineForMember(@Valid RoutineCreateRequest request, Long trainerId,
      Long memberId) {
    trainerService.findById(trainerId);
    boolean myClient = ptContractService.isMyClient(trainerId, memberId);

    if (myClient) {
      return createRoutine(request, memberId);
    }

    throw new RestApiException(WorkoutErrorCode.NOT_ALLOWED_ACCESS);
  }

}