package com.workout.body.repository;

import com.workout.body.domain.BodyComposition;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import com.workout.gym.domain.Gym;
import com.workout.gym.repository.GymRepository;
import com.workout.member.domain.AccountStatus;
import com.workout.member.domain.Gender;
import com.workout.member.domain.Role;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class BodyCompositionRepositoryTest {

    @Autowired
    private BodyCompositionRepository bodyCompositionRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private GymRepository gymRepository;

    @Test
    @DisplayName("사용자 ID로 BodyComposition 리스트 조회 - Builder 패턴 사용")
    void testFindByMemberIdWithBuilder() {
        // given
        Gym gym = gymRepository.save(Gym.builder()
                .name("Test Gym")
                .address("Seoul")
                .build());

        Member Member1 = memberRepository.save(Member.builder()
                .gym(gym)
                .name("홍길동")
                .email("Member1@workout.com")
                .password("password1")
                .gender(Gender.MALE)
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.MEMBER)
                .build());

        Member Member2 = memberRepository.save(Member.builder()
                .gym(gym)
                .name("김영희")
                .email("Member2@workout.com")
                .password("password2")
                .gender(Gender.FEMALE)
                .accountStatus(AccountStatus.ACTIVE)
                .role(Role.MEMBER)
                .build());

        BodyComposition body1 = BodyComposition.builder()
                .member(Member1)
                .measurementDate(LocalDate.now())
                .weightKg(70L)
                .fatKg(20L)
                .muscleMassKg(50L)
                .build();

        BodyComposition body2 = BodyComposition.builder()
                .member(Member1)
                .measurementDate(LocalDate.now().minusDays(1))
                .weightKg(72L)
                .fatKg(21L)
                .muscleMassKg(51L)
                .build();

        BodyComposition body3 = BodyComposition.builder()
                .member(Member2)
                .measurementDate(LocalDate.now())
                .weightKg(68L)
                .fatKg(19L)
                .muscleMassKg(48L)
                .build();

        bodyCompositionRepository.saveAll(List.of(body1, body2, body3));

        // when
        List<BodyComposition> result = bodyCompositionRepository.findByMemberId(Member1.getId());

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(bc -> bc.getMember().getId().equals(Member1.getId()));
    }
}
