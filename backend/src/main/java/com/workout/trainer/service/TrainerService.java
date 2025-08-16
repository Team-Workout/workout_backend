package com.workout.trainer.service;

import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Certification;
import com.workout.trainer.domain.Education;
import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.domain.TrainerSpecialty;
import com.workout.trainer.domain.Workexperiences;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.repository.AwardRepository;
import com.workout.trainer.repository.CertificationRepository;
import com.workout.trainer.repository.EducationRepository;
import com.workout.trainer.repository.SpecialtyRepository;
import com.workout.trainer.repository.TrainerRepository;
import com.workout.trainer.repository.TrainerSpecialtyRepository;
import com.workout.trainer.repository.WorkexperiencesRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TrainerService {

  private final TrainerRepository trainerRepository;
  private final AwardRepository awardRepository;
  private final CertificationRepository certificationRepository;
  private final EducationRepository educationRepository;
  private final WorkexperiencesRepository workexperiencesRepository;
  private final SpecialtyRepository specialtyRepository; // 의존성 추가
  private final TrainerSpecialtyRepository trainerSpecialtyRepository; // 의존성 추가

  public TrainerService(TrainerRepository trainerRepository, AwardRepository awardRepository,
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
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId));

    List<Award> awards = awardRepository.findAllByTrainerId(trainerId);
    List<Certification> certifications = certificationRepository.findAllByTrainerId(trainerId);
    List<Education> educations = educationRepository.findAllByTrainerId(trainerId);
    List<Workexperiences> workexperiences = workexperiencesRepository.findAllByTrainerId(trainerId);
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
    deleteProfileDetails(trainerId);
    saveProfileDetails(trainer, requestDto);
  }

  @Transactional
  public void deleteProfile(Long trainerId) {
    if (!trainerRepository.existsById(trainerId)) {
      throw new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId);
    }
    deleteProfileDetails(trainerId);
  }

  private void saveProfileDetails(Trainer trainer, ProfileCreateDto requestDto) {
    trainer.setIntroduction(requestDto.introduction());
    trainerRepository.save(trainer);

    List<Award> awards = requestDto.awards().stream().map(dto -> dto.toEntity(trainer)).toList();
    List<Certification> certifications = requestDto.certifications().stream()
        .map(dto -> dto.toEntity(trainer)).toList();
    List<Education> educations = requestDto.educations().stream().map(dto -> dto.toEntity(trainer))
        .toList();
    List<Workexperiences> workexperiences = requestDto.workExperiences().stream()
        .map(dto -> dto.toEntity(trainer)).toList();

    awardRepository.saveAll(awards);
    certificationRepository.saveAll(certifications);
    educationRepository.saveAll(educations);
    workexperiencesRepository.saveAll(workexperiences);

    handleSpecialties(trainer, requestDto.specialties());
  }

  private void deleteProfileDetails(Long trainerId) {
    awardRepository.deleteAllByTrainerId(trainerId);
    certificationRepository.deleteAllByTrainerId(trainerId);
    educationRepository.deleteAllByTrainerId(trainerId);
    workexperiencesRepository.deleteAllByTrainerId(trainerId);
    // Specialty 관계도 삭제
    trainerSpecialtyRepository.deleteAllByTrainerId(trainerId);
  }

  public List<ProfileResponseDto> getTrainerProfilesByGym(Long gymId) {
    List<Trainer> trainers = trainerRepository.findAllByGymId(gymId);

    return trainers.stream()
        .map(trainer -> getProfile(trainer.getId()))
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
}