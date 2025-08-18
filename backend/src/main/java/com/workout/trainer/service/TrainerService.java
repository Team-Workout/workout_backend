package com.workout.trainer.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.gym.domain.Gym;
import com.workout.gym.service.GymService;
import com.workout.member.service.MemberService;
import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Certification;
import com.workout.trainer.domain.Education;
import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.domain.TrainerSpecialty;
import com.workout.trainer.domain.Workexperience;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.dto.TrainerProfileDto;
import com.workout.trainer.dto.TrainerSpecialtyDto;
import com.workout.trainer.repository.AwardRepository;
import com.workout.trainer.repository.CertificationRepository;
import com.workout.trainer.repository.EducationRepository;
import com.workout.trainer.repository.SpecialtyRepository;
import com.workout.trainer.repository.TrainerRepository;
import com.workout.trainer.repository.TrainerSpecialtyRepository;
import com.workout.trainer.repository.WorkexperiencesRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TrainerService {

  private final TrainerRepository trainerRepository;
  private final AwardRepository awardRepository;
  private final CertificationRepository certificationRepository;
  private final EducationRepository educationRepository;
  private final WorkexperiencesRepository workexperiencesRepository;
  private final SpecialtyRepository specialtyRepository;
  private final TrainerSpecialtyRepository trainerSpecialtyRepository;
  private final GymService gymService;
  private final PasswordEncoder passwordEncoder;
  private final MemberService memberService;

  public TrainerService(
      TrainerRepository trainerRepository, AwardRepository awardRepository,
      CertificationRepository certificationRepository, EducationRepository educationRepository,
      WorkexperiencesRepository workexperiencesRepository, SpecialtyRepository specialtyRepository,
      TrainerSpecialtyRepository trainerSpecialtyRepository, GymService gymService,
      PasswordEncoder passwordEncoder, MemberService memberService) {
    this.trainerRepository = trainerRepository;
    this.awardRepository = awardRepository;
    this.certificationRepository = certificationRepository;
    this.educationRepository = educationRepository;
    this.workexperiencesRepository = workexperiencesRepository;
    this.specialtyRepository = specialtyRepository;
    this.trainerSpecialtyRepository = trainerSpecialtyRepository;
    this.gymService = gymService;
    this.passwordEncoder = passwordEncoder;
    this.memberService = memberService;
  }

  public ProfileResponseDto getProfile(Long trainerId) {
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId));

    List<Award> awards = awardRepository.findAllByTrainerId(trainerId);
    List<Certification> certifications = certificationRepository.findAllByTrainerId(trainerId);
    List<Education> educations = educationRepository.findAllByTrainerId(trainerId);
    List<Workexperience> workexperiences = workexperiencesRepository.findAllByTrainerId(trainerId);
    Set<Specialty> specialties = trainerSpecialtyRepository.findSpecialtiesByTrainerId(trainerId);

    return ProfileResponseDto.from(trainer, awards, certifications, educations, workexperiences,
        specialties);
  }

  @Transactional
  public void createProfile(Long trainerId, ProfileCreateDto requestDto) {
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId));
    saveProfileDetails(trainer, requestDto);
  }

  @Transactional
  public void updateProfile(Long trainerId, ProfileCreateDto requestDto) {
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId));

    trainer.setIntroduction(requestDto.introduction());

    // 각 프로필 항목에 대한 전용 업데이트 메소드를 순서대로 호출
    updateAwards(trainer, requestDto.awards());
    updateCertifications(trainer, requestDto.certifications());
    updateEducations(trainer, requestDto.educations());
    updateWorkexperiences(trainer, requestDto.workExperiences());

    // Specialty는 별도 핸들링
    trainerSpecialtyRepository.deleteAllByTrainerId(trainerId);
    handleSpecialties(trainer, requestDto.specialties());
  }

  private void updateAwards(Trainer trainer, List<ProfileCreateDto.AwardDto> awardDtos) {
    // 1. [기존 데이터 조회]
    List<Award> existingAwards = awardRepository.findAllByTrainerId(trainer.getId());

    // 2. [빠른 조회를 위한 준비] 조회한 엔티티 목록을 ID를 key로 하는 Map으로 변환합니다.
    //    이렇게 하면 특정 ID를 가진 Award를 O(1) 시간 복잡도로 찾을 수 있습니다.
    Map<Long, Award> existingAwardsMap = existingAwards.stream()
        .collect(Collectors.toMap(Award::getId, Function.identity()));

    // 3. [Create or Update] 클라이언트가 보낸 DTO 목록을 순회합니다.
    for (ProfileCreateDto.AwardDto dto : awardDtos) {
      if (dto.id() == null) {
        // [Create] DTO의 id가 null이면, 새로운 엔티티로 간주하고 생성하여 저장합니다.
        Award newAward = dto.toEntity(trainer);
        awardRepository.save(newAward);
      } else {
        // [Update] DTO의 id가 존재하면, Map에서 기존 엔티티를 찾습니다.
        Award existingAward = existingAwardsMap.get(dto.id());
        if (existingAward != null) {
          // 엔티티를 찾았다면, DTO의 내용으로 필드를 업데이트합니다.
          existingAward.updateDetails(dto.awardName(), dto.awardDate(), dto.awardPlace());
          // '처리된' 엔티티이므로, Map에서 제거합니다.
          existingAwardsMap.remove(dto.id());
        }
        // 만약 existingAward가 null이라면? 클라이언트가 존재하지 않는 ID로 수정을 요청한 경우입니다.
        // 이 경우, 요구사항에 따라 예외를 던지거나(엄격한 방식) 무시할(관대한 방식) 수 있습니다. 현재는 무시됩니다.
      }
    }

    // 4. [Delete] DTO 목록 처리가 끝난 후 Map에 여전히 남아있는 엔티티들이 있습니다.
    //    이 엔티티들은 클라이언트가 보낸 목록에 포함되지 않은, 즉 '삭제' 대상입니다.
    if (!existingAwardsMap.isEmpty()) {
      awardRepository.deleteAll(existingAwardsMap.values());
    }
  }

  private void updateCertifications(Trainer trainer,
      List<ProfileCreateDto.CertificationDto> certificationDtos) {
    // 1. [기존 데이터 조회]
    List<Certification> existingCertifications = certificationRepository.findAllByTrainerId(
        trainer.getId());

    // 2. [빠른 조회를 위한 준비]
    Map<Long, Certification> existingCertificationsMap = existingCertifications.stream()
        .collect(Collectors.toMap(Certification::getId, Function.identity()));

    // 3. [Create or Update]
    for (ProfileCreateDto.CertificationDto dto : certificationDtos) {
      if (dto.id() == null) {
        // [Create]
        Certification newCertification = dto.toEntity(trainer);
        certificationRepository.save(newCertification);
      } else {
        // [Update]
        Certification existingCertification = existingCertificationsMap.get(dto.id());
        if (existingCertification != null) {
          existingCertification.updateDetails(dto.certificationName(), dto.issuingOrganization(),
              dto.acquisitionDate());
          existingCertificationsMap.remove(dto.id());
        }
      }
    }

    // 4. [Delete]
    if (!existingCertificationsMap.isEmpty()) {
      certificationRepository.deleteAll(existingCertificationsMap.values());
    }
  }

  public void updateEducations(Trainer trainer, List<ProfileCreateDto.EducationDto> educationDtos) {
    // 1. [기존 데이터 조회]
    List<Education> existingEducations = educationRepository.findAllByTrainerId(trainer.getId());

    // 2. [빠른 조회를 위한 준비]
    Map<Long, Education> existingEducationsMap = existingEducations.stream()
        .collect(Collectors.toMap(Education::getId, Function.identity()));

    // 3. [Create or Update]
    for (ProfileCreateDto.EducationDto dto : educationDtos) {
      if (dto.id() == null) {
        // [Create]
        Education newEducation = dto.toEntity(trainer);
        educationRepository.save(newEducation);
      } else {
        // [Update]
        Education existingEducation = existingEducationsMap.get(dto.id());
        if (existingEducation != null) {
          existingEducation.updateDetails(dto.schoolName(), dto.educationName(), dto.degree(),
              dto.startDate(), dto.endDate());
          existingEducationsMap.remove(dto.id());
        }
      }
    }

    // 4. [Delete]
    if (!existingEducationsMap.isEmpty()) {
      educationRepository.deleteAll(existingEducationsMap.values());
    }
  }

  public void updateWorkexperiences(Trainer trainer,
      List<ProfileCreateDto.WorkExperienceDto> workExperienceDtos) {
    // 1. [기존 데이터 조회]
    List<Workexperience> existingWorkexperiences = workexperiencesRepository.findAllByTrainerId(
        trainer.getId());

    // 2. [빠른 조회를 위한 준비]
    Map<Long, Workexperience> existingWorkexperiencesMap = existingWorkexperiences.stream()
        .collect(Collectors.toMap(Workexperience::getId, Function.identity()));

    // 3. [Create or Update]
    for (ProfileCreateDto.WorkExperienceDto dto : workExperienceDtos) {
      if (dto.id() == null) {
        // [Create]
        Workexperience newWorkexperience = dto.toEntity(trainer);
        workexperiencesRepository.save(newWorkexperience);
      } else {
        // [Update]
        Workexperience existingWorkexperience = existingWorkexperiencesMap.get(dto.id());
        if (existingWorkexperience != null) {
          existingWorkexperience.updateDetails(dto.workName(), dto.workPlace(), dto.workPosition(),
              dto.workStartDate(), dto.workEndDate());
          existingWorkexperiencesMap.remove(dto.id());
        }
      }
    }

    // 4. [Delete]
    if (!existingWorkexperiencesMap.isEmpty()) {
      workexperiencesRepository.deleteAll(existingWorkexperiencesMap.values());
    }
  }

  @Transactional
  public void deleteProfile(Long trainerId) {
    if (!trainerRepository.existsById(trainerId)) {
      throw new EntityNotFoundException("삭제할 트레이너를 찾을 수 없습니다. ID: " + trainerId);
    }

    // 하위 엔티티들 먼저 삭제
    deleteProfileDetails(trainerId);

    // 마지막으로 Trainer 엔티티 자체를 삭제 (이 부분이 누락되었습니다)
    trainerRepository.deleteById(trainerId);
  }

  private void deleteProfileDetails(Long trainerId) {
    awardRepository.deleteAllByTrainerId(trainerId);
    certificationRepository.deleteAllByTrainerId(trainerId);
    educationRepository.deleteAllByTrainerId(trainerId);
    workexperiencesRepository.deleteAllByTrainerId(trainerId);
    trainerSpecialtyRepository.deleteAllByTrainerId(trainerId);
  }

  private void saveProfileDetails(Trainer trainer, ProfileCreateDto requestDto) {
    trainer.setIntroduction(requestDto.introduction());
    trainerRepository.save(trainer);

    List<Award> awards = requestDto.awards().stream().map(dto -> dto.toEntity(trainer)).toList();
    List<Certification> certifications = requestDto.certifications().stream()
        .map(dto -> dto.toEntity(trainer)).toList();
    List<Education> educations = requestDto.educations().stream().map(dto -> dto.toEntity(trainer))
        .toList();
    List<Workexperience> workexperiences = requestDto.workExperiences().stream()
        .map(dto -> dto.toEntity(trainer)).toList();

    awardRepository.saveAll(awards);
    certificationRepository.saveAll(certifications);
    educationRepository.saveAll(educations);
    workexperiencesRepository.saveAll(workexperiences);

    handleSpecialties(trainer, requestDto.specialties());
  }

  public List<ProfileResponseDto> getTrainerProfilesByGym(Long gymId) {
    // 1. JPQL을 통해 모든 정보를 DTO 리스트로 한번에 조회 (단일 쿼리)
    List<TrainerProfileDto> flatResults = trainerRepository.findTrainerProfilesByGymIdAsFlatDto(
        gymId);

    if (flatResults.isEmpty()) {
      return Collections.emptyList();
    }

    // 2. 조회된 트레이너들의 ID 목록을 추출 (중복 제거)
    List<Long> trainerIds = flatResults.stream()
        .map(TrainerProfileDto::trainerId)
        .distinct()
        .toList();

    // 3. Specialty 정보를 IN 쿼리로 한번에 조회 후, trainerId 기준으로 그룹핑 (쿼리 1번)
    List<TrainerSpecialtyDto> specialtyDtos = trainerSpecialtyRepository.findSpecialtiesByTrainerIds(
        trainerIds);

    Map<Long, Set<String>> specialtiesMap = specialtyDtos.stream()
        .collect(Collectors.groupingBy(
            TrainerSpecialtyDto::trainerId,
            Collectors.mapping(TrainerSpecialtyDto::specialtyName, Collectors.toSet())
        ));

    // 4. trainerId를 기준으로 DTO들을 그룹핑 (메모리에서 수행)
    Map<Long, List<TrainerProfileDto>> groupedByTrainerId = flatResults.stream()
        .collect(Collectors.groupingBy(TrainerProfileDto::trainerId));

    // 5. 그룹핑된 데이터를 최종 ProfileResponseDto 형태로 조립
    return groupedByTrainerId.values().stream()
        .map(trainerGroup -> {
          TrainerProfileDto first = trainerGroup.get(0);
          Long currentTrainerId = first.trainerId();

          // AwardDto 리스트 생성
          List<ProfileResponseDto.AwardDto> awards = trainerGroup.stream()
              .filter(dto -> dto.awardName() != null)
              .map(dto -> new ProfileResponseDto.AwardDto(dto.awardId(), dto.awardName(),
                  dto.awardDate(),
                  dto.awardPlace()))
              .distinct()
              .toList();

          // CertificationDto 리스트 생성
          List<ProfileResponseDto.CertificationDto> certifications = trainerGroup.stream()
              .filter(dto -> dto.certificationName() != null)
              .map(dto -> new ProfileResponseDto.CertificationDto(dto.certificationId(),
                  dto.certificationName(),
                  dto.issuingOrganization(), dto.acquisitionDate()))
              .distinct()
              .toList();

          // EducationDto 리스트 생성
          List<ProfileResponseDto.EducationDto> educations = trainerGroup.stream()
              .filter(dto -> dto.schoolName() != null)
              .map(dto -> new ProfileResponseDto.EducationDto(dto.educationId(), dto.schoolName(),
                  dto.educationName(),
                  dto.degree(), dto.startDate(), dto.endDate()))
              .distinct()
              .toList();

          // WorkExperienceDto 리스트 생성
          List<ProfileResponseDto.WorkExperienceDto> workExperiences = trainerGroup.stream()
              .filter(dto -> dto.workName() != null)
              .map(dto -> new ProfileResponseDto.WorkExperienceDto(dto.workExperienceId(),
                  dto.workName(), dto.workPlace(),
                  dto.workPosition(), dto.workStart(), dto.workEnd()))
              .distinct()
              .toList();

          // 해당 트레이너의 Specialty Set을 Map에서 조회
          Set<String> specialties = specialtiesMap.getOrDefault(currentTrainerId,
              Collections.emptySet());

          // 최종 DTO 반환
          return new ProfileResponseDto(
              first.trainerId(),
              first.name(),
              first.email(),
              first.introduction(),
              awards,
              certifications,
              educations,
              workExperiences,
              specialties // 조회한 Specialty 정보 채워넣기
          );
        })
        .collect(Collectors.toList());
  }

  private void handleSpecialties(Trainer trainer, Set<String> specialtyNames) {
    if (specialtyNames == null || specialtyNames.isEmpty()) {
      return;
    }

    //기존 db에서
    Set<Specialty> existingSpecialties = specialtyRepository.findByNameIn(specialtyNames);
    Set<String> existingNames = existingSpecialties.stream()
        .map(Specialty::getName)
        .collect(Collectors.toSet());

    Set<Specialty> newSpecialties = specialtyNames.stream()
        .filter(name -> !existingNames.contains(name))
        .map(name -> Specialty.builder().name(name).build())
        .collect(Collectors.toSet());

    if (!newSpecialties.isEmpty()) {
      specialtyRepository.saveAll(newSpecialties);
    }

    existingSpecialties.addAll(newSpecialties);

    List<TrainerSpecialty> trainerSpecialties = existingSpecialties.stream()
        .map(specialty -> TrainerSpecialty.builder().trainer(trainer).specialty(specialty).build())
        .toList();

    trainerSpecialtyRepository.saveAll(trainerSpecialties);
  }

  public Trainer registerTrainer(@Valid SignupRequest signupRequest) {
    Gym gym = gymService.findById(signupRequest.gymId());

    memberService.ensureEmailIsUnique(signupRequest.email());

    String encodedPassword = passwordEncoder.encode(signupRequest.password());

    Trainer trainer = signupRequest.toTrainerEntity(gym, encodedPassword);

    return trainerRepository.save(trainer);
  }
}