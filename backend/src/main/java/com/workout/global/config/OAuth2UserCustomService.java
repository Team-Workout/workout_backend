package com.workout.global.config;

import com.workout.member.domain.Role;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;

  @Transactional
  @Override
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User user = super.loadUser(userRequest);
    saveOrUpdate(user);
    return user;
  }

  private Member saveOrUpdate(OAuth2User oAuth2User) {
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = (String) attributes.get("email");
    String name = (String) attributes.get("name");

    return memberRepository.findByEmail(email)
        .map(entity -> {
          entity.updateName(name);
          return entity;
        })
        .orElseGet(() -> {
          Member newMember = Member.builder()
              .email(email)
              .name(name)
              .password("")
              .role(Role.MEMBER)
              .build();
          return memberRepository.save(newMember);
        });
  }
}
