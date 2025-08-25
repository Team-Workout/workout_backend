package com.workout.pt.service.contract;

import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.PTErrorCode;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.domain.contract.PTOfferingStatus;
import com.workout.pt.dto.request.OfferingCreateRequest;
import com.workout.pt.dto.response.PtOfferingResponse;
import com.workout.pt.repository.PTOfferingRepository;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.service.TrainerService;
import org.springframework.stereotype.Service;

@Service
public class PTOfferingService {

  private final PTOfferingRepository ptOfferingRepository;
  private final TrainerService trainerService;

  public PTOfferingService(PTOfferingRepository ptOfferingRepository,
      TrainerService trainerService) {
    this.ptOfferingRepository = ptOfferingRepository;
    this.trainerService = trainerService;
  }

  public void register(OfferingCreateRequest request, Long userId) {
    Trainer trainer = trainerService.findById(userId);

    PTOffering offering = PTOffering.builder()
        .trainer(trainer) // Member를 Trainer로 캐스팅
        .title(request.title())
        .gym(trainer.getGym())
        .description(request.description())
        .price(request.price())
        .totalSessions(request.totalSessions())
        .status(PTOfferingStatus.ACTIVE)
        .build();

    ptOfferingRepository.save(offering);
  }

  public void delete(Long offeringId, Long userId) {
    Trainer trainer = trainerService.findById(userId);
    PTOffering ptOffering = ptOfferingRepository.findById(offeringId).orElseThrow(
        () -> new RestApiException(PTErrorCode.NOT_FOUND_PT_OFFERING));

    if (!ptOffering.getTrainer().getId().equals(trainer.getId())) {
      throw new RestApiException(PTErrorCode.NOT_ALLOWED_ACCESS);
    }

    ptOfferingRepository.delete(ptOffering);
  }

  public PtOfferingResponse findbyTrainerId(Long trainerId) {
    trainerService.findById(trainerId);

    return PtOfferingResponse.from(ptOfferingRepository.findAllByTrainerId(trainerId));
  }
}
