package com.workout.pt.dto.response;

import com.workout.pt.domain.contract.PTOffering;
import java.util.List;

public record PtOfferingResponse(
    List<PTOffering> ptOfferings
) {

  public static PtOfferingResponse from(List<PTOffering> ptOfferings) {
    return new PtOfferingResponse(ptOfferings);
  }
}
