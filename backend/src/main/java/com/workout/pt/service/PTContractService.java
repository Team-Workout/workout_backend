package com.workout.pt.service;

import com.workout.pt.domain.contract.PTApplication;
import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.repository.PTContractRepository;
import com.workout.pt.repository.PTOfferingRepository;
import com.workout.trainer.repository.TrainerRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class PTContractService {

  private final TrainerRepository trainerRepository;
  private final PTContractRepository ptContractRepository;
  private final PTOfferingRepository ptOfferingRepository;

  public PTContractService(TrainerRepository trainerRepository,
      PTContractRepository ptContractRepository, PTOfferingRepository ptOfferingRepository) {
    this.trainerRepository = trainerRepository;
    this.ptContractRepository = ptContractRepository;
    this.ptOfferingRepository = ptOfferingRepository;
  }

  public void createContractFromApplication(PTApplication application) {

    PTOffering ptOffering = application.getOffering();

    PTContract contract = PTContract.builder()
        .application(application) // 어떤 신청으로부터 계약이 생성되었는지 연결
        .member(application.getMember())
        .trainer(ptOffering.getTrainer())
        .gym(ptOffering.getGym())
        .price(ptOffering.getPrice()) // 신청 시의 가격 정보 사용
        .paymentDate(LocalDate.now()) //임시 로직
        .totalSessions(ptOffering.getTotalSessions()) // 신청 시의 세션 횟수
        .remainingSessions(ptOffering.getTotalSessions()) // 남은 횟수는 전체 횟수와 동일하게 시작
        .status(PTContractStatus.ACTIVE) // 계약 생성 시 상태는 '진행 중'
        .build();

    ptContractRepository.save(contract);
  }

  public void cancelContractByMemberId(Long memberId) {

  }

  public void cancelContractByTrainerId(Long trainerId) {

  }
}
