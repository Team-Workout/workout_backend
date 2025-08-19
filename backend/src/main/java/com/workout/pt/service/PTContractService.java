package com.workout.pt.service;

import com.workout.pt.domain.PTApplication;
import com.workout.pt.domain.PTContract;
import com.workout.pt.domain.PTContractStatus;
import com.workout.pt.repository.PTContractRepository;
import com.workout.trainer.repository.TrainerRepository;
import java.time.LocalDate;
import org.springframework.stereotype.Service;

@Service
public class PTContractService {

  private final TrainerRepository trainerRepository;
  private final PTContractRepository ptContractRepository;

  public PTContractService(TrainerRepository trainerRepository, PTContractRepository ptContractRepository) {
    this.trainerRepository = trainerRepository;
    this.ptContractRepository = ptContractRepository;
  }

  public void createContractFromApplication(PTApplication application) {
    // 비즈니스 규칙: 계약 시작일은 오늘, 만료일은 90일 뒤로 가정
    LocalDate startDate = LocalDate.now();
    LocalDate expiryDate = startDate.plusDays(90);

    PTContract contract = PTContract.builder()
        .application(application) // 어떤 신청으로부터 계약이 생성되었는지 연결
        .member(application.getMember())
        .trainer(application.getTrainer())
        .gym(application.getGym())
        .price(application.getPrice()) // 신청 시의 가격 정보 사용
        .paymentDate(LocalDate.now())
        .startDate(startDate)
        .expiryDate(expiryDate)
        .totalSessions(application.getTotalSessions()) // 신청 시의 세션 횟수
        .remainingSessions(application.getTotalSessions()) // 남은 횟수는 전체 횟수와 동일하게 시작
        .status(PTContractStatus.ACTIVE) // 계약 생성 시 상태는 '진행 중'
        .build();

    ptContractRepository.save(contract);
  }
  void apply() {

  }

  void accept() {

  }

  void reject() {

  }
}
