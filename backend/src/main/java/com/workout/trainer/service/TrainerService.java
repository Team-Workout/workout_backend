package com.workout.trainer.service;

import com.workout.trainer.domain.Trainer;
import com.workout.trainer.dto.ProfileCreateDto;
import com.workout.trainer.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TrainerService {
  private final TrainerRepository trainerRepository;

  public TrainerService(TrainerRepository trainerRepository) {
    this.trainerRepository = trainerRepository;
  }

  @Transactional
  public void createProfile(Long trainerId, ProfileCreateDto requestDto) {
    Trainer user = trainerRepository.findById(trainerId)
        .orElseThrow(() -> new EntityNotFoundException("트레이너를 찾을 수 없습니다. ID: " + trainerId));


}


}
