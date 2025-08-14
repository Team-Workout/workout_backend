package com.workout.trainer.service;

import com.workout.trainer.domain.Award;
import com.workout.trainer.domain.Certification;
import com.workout.trainer.domain.Education;
import com.workout.trainer.domain.Specialty;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.domain.Workexperiences;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.repository.AwardRepository;
import com.workout.trainer.repository.CertificationRepository;
import com.workout.trainer.repository.EducationRepository;
import com.workout.trainer.repository.TrainerRepository;
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

  public TrainerService(TrainerRepository trainerRepository, AwardRepository awardRepository,
      CertificationRepository certificationRepository, EducationRepository educationRepository,
      WorkexperiencesRepository workexperiencesRepository) {
    this.trainerRepository = trainerRepository;
    this.awardRepository = awardRepository;
    this.certificationRepository = certificationRepository;
    this.educationRepository = educationRepository;
    this.workexperiencesRepository = workexperiencesRepository;
  }

  @Transactional
  public void createProfile(Long trainerId, ProfileCreateDto requestDto) {
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId));

    trainer.setIntroduction(requestDto.introduction());

    awardRepository.deleteAllByTrainerId(trainerId);
    certificationRepository.deleteAllByTrainerId(trainerId);
    educationRepository.deleteAllByTrainerId(trainerId);
    workexperiencesRepository.deleteAllByTrainerId(trainerId);

    if (requestDto.awards() != null) {
      List<Award> awards = requestDto.awards().stream()
          .map(awardDto -> Award.of(awardDto, trainer))
          .collect(Collectors.toList());
      awardRepository.saveAll(awards);
    }

    if (requestDto.certifications() != null) {
      List<Certification> certifications = requestDto.certifications().stream()
          .map(certDto -> Certification.of(certDto, trainer)).collect(Collectors.toList());
      certificationRepository.saveAll(certifications);
    }

    if (requestDto.educations() != null) {
      List<Education> educations = requestDto.educations().stream()
          .map(eduDto -> Education.of(eduDto, trainer)).collect(Collectors.toList());
      educationRepository.saveAll(educations);
    }

    if (requestDto.workExperiences() != null) {
      List<Workexperiences> workexperiences = requestDto.workExperiences().stream()
          .map(workDto -> Workexperiences.of(workDto, trainer)).collect(Collectors.toList());
      workexperiencesRepository.saveAll(workexperiences);
    }

    trainerRepository.save(trainer);
  }

  public ProfileResponseDto getProfile(Long trainerId) {
    Trainer trainer = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId));

    List<Award> awards = awardRepository.findByTrainerId(trainerId);
    List<Certification> certifications = certificationRepository.findByTrainerId(trainerId);
    List<Education> educations = educationRepository.findByTrainerId(trainerId);
    List<Workexperiences> workExperiences = workexperiencesRepository.findByTrainerId(trainerId);
    Set<Specialty> specialties = trainer.getSpecialties(); // Trainer 엔티티에서 직접 가져옴

    return ProfileResponseDto.from(trainer, awards, certifications, educations, workExperiences, specialties);
  }

}
