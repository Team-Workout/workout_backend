package com.workout.trainer.service;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.global.exception.errorcode.ProfileErrorCode;
import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Certification;
import com.workout.trainer.domain.Education;
import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.domain.TrainerSpecialty;
import com.workout.trainer.domain.Workexperience;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.repository.AwardRepository;
import com.workout.trainer.repository.CertificationRepository;
import com.workout.trainer.repository.EducationRepository;
import com.workout.trainer.repository.SpecialtyRepository;
import com.workout.trainer.repository.TrainerRepository;
import com.workout.trainer.repository.TrainerSpecialtyRepository;
import com.workout.trainer.repository.WorkexperiencesRepository;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TrainerProfileService {

  private final TrainerRepository trainerRepository;
  private final AwardRepository awardRepository;
  private final CertificationRepository certificationRepository;
  private final EducationRepository educationRepository;
  private final WorkexperiencesRepository workexperiencesRepository;
  private final SpecialtyRepository specialtyRepository;
  private final TrainerSpecialtyRepository trainerSpecialtyRepository;

  public TrainerProfileService(TrainerRepository trainerRepository, AwardRepository awardRepository,
      CertificationRepository certificationRepository, EducationRepository educationRepository,
      WorkexperiencesRepository workexperiencesRepository, SpecialtyRepository specialtyRepository,
      TrainerSpecialtyRepository trainerSpecialtyRepository) {
    this.trainerRepository = trainerRepository;
    this.awardRepository = awardRepository;
    this.certificationRepository = certificationRepository;
    this.educationRepository = educationRepository;
    this.workexperiencesRepository = workexperiencesRepository;
    this.specialtyRepository = specialtyRepository;
    this.trainerSpecialtyRepository = trainerSpecialtyRepository;
  }

  public ProfileResponseDto getProfile(Long trainerId) {
    Trainer trainer = trainerRepository.findByIdWithDetails(trainerId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));
    return ProfileResponseDto.fromEntity(trainer);
  }

  @Transactional
  public void createProfile(Long trainerId, ProfileCreateDto requestDto) {
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new RestApiException(ProfileErrorCode.NOT_FOUND_PROFILE));
    saveProfileDetails(trainer, requestDto);
  }

  @Transactional
  public void updateProfile(Long trainerId, ProfileCreateDto requestDto) {
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));

    trainer.setIntroduction(requestDto.introduction());

    // 각 프로필 항목 업데이트 로직 호출 (이하 모든 private 메소드들 TrainerService에서 그대로 이동)
    updateAwards(trainer, requestDto.awards());
    updateCertifications(trainer, requestDto.certifications());
    updateEducations(trainer, requestDto.educations());
    updateWorkexperiences(trainer, requestDto.workExperiences());

    trainerSpecialtyRepository.deleteAllByTrainerId(trainerId);
    handleSpecialties(trainer, requestDto.specialties());
  }

  @Transactional
  public void deleteAllProfileDetails(Long trainerId) {
    awardRepository.deleteAllByTrainerId(trainerId);
    certificationRepository.deleteAllByTrainerId(trainerId);
    educationRepository.deleteAllByTrainerId(trainerId);
    workexperiencesRepository.deleteAllByTrainerId(trainerId);
    trainerSpecialtyRepository.deleteAllByTrainerId(trainerId);
  }

  private void updateAwards(Trainer trainer, Set<ProfileCreateDto.AwardDto> awardDtos) {
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
      Set<ProfileCreateDto.CertificationDto> certificationDtos) {
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

  public void updateEducations(Trainer trainer, Set<ProfileCreateDto.EducationDto> educationDtos) {
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
      Set<ProfileCreateDto.WorkExperienceDto> workExperienceDtos) {
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

}