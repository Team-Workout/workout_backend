package com.workout.pt.service.contract;

import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.dto.response.ClientListResponse.MemberResponse;
import com.workout.pt.repository.PTContractRepository;
import com.workout.trainer.domain.Trainer;
import com.workout.trainer.service.TrainerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PTTrainerService {

  private final PTContractRepository ptContractRepository;
  private final TrainerService trainerService;

  public PTTrainerService(PTContractRepository ptContractRepository,
      TrainerService trainerService) {
    this.ptContractRepository = ptContractRepository;
    this.trainerService = trainerService;
  }

  public Page<MemberResponse> findMyClients(Long trainerId, Pageable pageable) {

    Trainer trainer = trainerService.findById(trainerId);

    Page<PTContract> contractsPage = ptContractRepository
        .findByTrainerIdAndStatus(trainer.getId(), PTContractStatus.ACTIVE, pageable);

    return contractsPage.map(contract -> MemberResponse.from(contract.getMember()));
  }
}
