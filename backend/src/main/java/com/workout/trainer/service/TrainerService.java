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
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

  public List<ProfileResponseDto> getTrainerProfilesByGym(Long gymId) {
    // 1. gymId로 소속된 모든 트레이너를 한 번의 쿼리로 조회
    List<Trainer> trainers = trainerRepository.findProfilesByGymId(gymId);
    if (trainers.isEmpty()) {
      return Collections.emptyList();
    }


    List<Long> trainerIds = trainers.stream()
        .map(Trainer::getId)
        .collect(Collectors.toList());

    // 3. 트레이너 ID 목록(IN 절)을 사용하여 각 프로필 상세 정보들을 한 번의 쿼리로 모두 가져옴
    List<Award> awards = awardRepository.findByTrainerIdIn(trainerIds);
    List<Certification> certifications = certificationRepository.findByTrainerIdIn(trainerIds);
    List<Education> educations = educationRepository.findByTrainerIdIn(trainerIds);
    List<Workexperiences> workExperiences = workexperiencesRepository.findByTrainerIdIn(trainerIds);
    // Specialty는 Trainer 엔티티에 @ElementCollection 등으로 매핑되어 있다고 가정,
    // 이 경우 trainers 조회 시 함께 Eager 또는 Fetch Join으로 가져오는 것이 효율적입니다.
    // 현재는 개별 엔티티이므로 Trainer 객체에서 직접 가져옵니다.

    // 4. 조회된 상세 정보들을 trainerId를 key로 하는 Map으로 변환하여 조립 준비 (성능 최적화)
    Map<Long, List<Award>> awardsByTrainerId = awards.stream()
        .collect(Collectors.groupingBy(award -> award.getTrainer().getId()));
    Map<Long, List<Certification>> certsByTrainerId = certifications.stream()
        .collect(Collectors.groupingBy(cert -> cert.getTrainer().getId()));
    Map<Long, List<Education>> edusByTrainerId = educations.stream()
        .collect(Collectors.groupingBy(edu -> edu.getTrainer().getId()));
    Map<Long, List<Workexperiences>> worksByTrainerId = workExperiences.stream()
        .collect(Collectors.groupingBy(work -> work.getTrainer().getId()));

    // 5. 트레이너 목록을 순회하며, 메모리에 있는 데이터들을 조합하여 최종 DTO 리스트 생성
    return trainers.stream()
        .map(trainer -> ProfileResponseDto.from(
            trainer,
            awardsByTrainerId.getOrDefault(trainer.getId(), Collections.emptyList()),
            certsByTrainerId.getOrDefault(trainer.getId(), Collections.emptyList()),
            edusByTrainerId.getOrDefault(trainer.getId(), Collections.emptyList()),
            worksByTrainerId.getOrDefault(trainer.getId(), Collections.emptyList()),
            trainer.getSpecialties()
        ))
        .collect(Collectors.toList());
  }
}
