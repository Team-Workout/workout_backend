package com.workout.trainer.service;

import com.workout.auth.dto.SignupRequest;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.MemberErrorCode;
import com.workout.gym.domain.Gym;
import com.workout.pt.service.contract.PTContractService;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.dto.ProfileResponseDto;
import com.workout.trainer.repository.TrainerRepository;
import com.workout.utils.service.FileService;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class TrainerService {

  private final TrainerRepository trainerRepository;
  private final TrainerProfileService trainerProfileService; // [신규 주입]
  private final PTContractService ptContractService;
  private final FileService fileService;
  private final PasswordEncoder passwordEncoder;

  public TrainerService(
      TrainerRepository trainerRepository, FileService fileService,
      PasswordEncoder passwordEncoder, TrainerProfileService trainerProfileService,
      PTContractService ptContractService) {
    this.trainerRepository = trainerRepository;
    this.fileService = fileService;
    this.passwordEncoder = passwordEncoder;
    this.trainerProfileService = trainerProfileService;
    this.ptContractService = ptContractService;
  }

  public Long createTrainer(SignupRequest request, Gym gym) {
    String encodedPassword = passwordEncoder.encode(request.password());
    Trainer trainer = request.toTrainerEntity(gym, encodedPassword);

    trainer.setProfileImageUri(fileService.getDefaultProfileImageUrl());

    return trainerRepository.save(trainer).getId();
  }

  @Transactional
  public void deleteTrainerAccount(Long trainerId) {
    Trainer trainer = findById(trainerId);

    String profileImageUri = trainer.getProfileImageUri();

    if (profileImageUri != null && !profileImageUri.equals(
        fileService.getDefaultProfileImageUrl())) {
      fileService.deleteProfileImageFile(profileImageUri);
    }

    trainerProfileService.deleteAllProfileDetails(trainerId);

    ptContractService.terminateAllContractsForTrainer(trainerId); // (신규 호출)

    trainerRepository.delete(trainer); // (deleteById -> delete(entity)로 변경하는 것이 안전)
  }

  public Page<ProfileResponseDto> getTrainerProfilesByGym(Long gymId, Pageable pageable) {
    Page<Trainer> trainerPage = trainerRepository.findAllByGymId(gymId, pageable);
    List<Trainer> trainersOnPage = trainerPage.getContent();

    if (trainersOnPage.isEmpty()) {
      return Page.empty(pageable);
    }

    List<Long> trainerIds = trainersOnPage.stream().map(Trainer::getId).toList();

    List<Trainer> trainersWithDetails = trainerRepository.findByIdInWithDetails(trainerIds);

    Map<Long, Trainer> trainerDetailMap = trainersWithDetails.stream()
        .collect(Collectors.toMap(Trainer::getId, Function.identity()));

    return trainerPage.map(trainer -> {
      Trainer detailedTrainer = trainerDetailMap.get(trainer.getId());

      return ProfileResponseDto.fromEntity(detailedTrainer);
    });
  }

  public Trainer findById(Long userId) {
    return trainerRepository.findById(userId)
        .orElseThrow(() -> new RestApiException(MemberErrorCode.MEMBER_NOT_FOUND));
  }
}