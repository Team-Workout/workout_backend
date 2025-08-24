package com.workout.pt.dto.response;

import com.workout.pt.domain.contract.PTContract;
import com.workout.pt.domain.contract.PTContractStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public record ContractResponse(
    Long contractId,
    Long memberId,
    String memberName, // 예시 필드
    Long trainerId,
    String trainerName, // 예시 필드
    PTContractStatus status,
    LocalDate startDate,
    Long totalSessions,
    Long remainingSessions
) {
  public static ContractResponse from(PTContract contract) {
    return new ContractResponse(
        contract.getId(),
        contract.getMember().getId(),
        contract.getMember().getName(), // Member 엔티티에 getName()이 있다고 가정
        contract.getTrainer().getId(),
        contract.getTrainer().getName(), // Trainer 엔티티에 getName()이 있다고 가정
        contract.getStatus(),
        contract.getStartDate(),
        contract.getTotalSessions(),
        contract.getRemainingSessions()
    );
  }

  public static List<ContractResponse> from(List<PTContract> contracts) {
    return contracts.stream()
        .map(ContractResponse::from)
        .collect(Collectors.toList());
  }
}
