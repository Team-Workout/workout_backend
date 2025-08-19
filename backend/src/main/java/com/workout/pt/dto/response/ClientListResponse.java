package com.workout.pt.dto.response;

import com.workout.member.domain.Member;
import java.util.List;

public record ClientListResponse(
    List<Member> clients
) {

  public static ClientListResponse from(List<Member> clients) {
    return new ClientListResponse(clients);
  }
}
