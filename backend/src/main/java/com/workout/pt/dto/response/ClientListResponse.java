package com.workout.pt.dto.response;

import com.workout.member.domain.Gender;
import com.workout.member.domain.Member;
import java.util.List;
import org.springframework.data.domain.Page;

public record ClientListResponse(
    List<MemberResponse> clients
) {

  public static ClientListResponse from(List<Member> clients) {
    return new ClientListResponse(
        clients.stream()
            .map(MemberResponse::from)
            .toList()
    );
  }

  public record MemberResponse(
      Long id,
      Long gymId,
      String gymName,
      String name,
      Gender gender
  ) {
    public static MemberResponse from(Member member) {

      return new MemberResponse(
          member.getId(),
          member.getGym().getId(),
          member.getGym().getName(),
          member.getName(),
          member.getGender()
      );
    }
  }
}
