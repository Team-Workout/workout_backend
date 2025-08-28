package com.workout.pt.dto.response;

import com.workout.pt.domain.contract.PTOffering;
import com.workout.pt.domain.contract.PTOfferingStatus;
import java.util.List;

public record PtOfferingResponse(
    Long id,
    Long gymId,
    String trainerName,
    String description,
    Long price,
    Long totalSessions,
    String title,
    PTOfferingStatus status
) {

  public static PtOfferingResponse from(PTOffering ptOffering) {
    return new PtOfferingResponse(
        ptOffering.getId(),
        ptOffering.getGym().getId(),
        ptOffering.getTrainer().getName(),
        ptOffering.getDescription(),
        ptOffering.getPrice(),
        ptOffering.getTotalSessions(),
        ptOffering.getTitle(),
        ptOffering.getStatus()
    );
  }
}
