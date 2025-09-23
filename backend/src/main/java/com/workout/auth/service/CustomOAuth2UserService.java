package com.workout.auth.service;

import com.workout.auth.domain.UserPrincipal;
import com.workout.auth.dto.SocialSignupInfo;
import com.workout.member.domain.Member;
import com.workout.member.repository.MemberRepository;
import jakarta.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

  private final MemberRepository memberRepository;
  private final HttpSession httpSession;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);
    Map<String, Object> attributes = oAuth2User.getAttributes();
    String email = (String) attributes.get("email");

    // 1. DB에서 사용자를 조회
    Optional<Member> optionalMember = memberRepository.findByEmail(email);

    if (optionalMember.isPresent()) {
      // 2-1. 기존 사용자가 존재하면, UserPrincipal을 반환하여 로그인 성공 처리
      Member member = optionalMember.get();
      return new UserPrincipal(member, attributes);
    } else {
      // 2-2. 신규 사용자면, 임시 정보를 세션에 저장
      String name = (String) attributes.get("name");
      String provider = userRequest.getClientRegistration().getRegistrationId();
      httpSession.setAttribute("socialSignupInfo", new SocialSignupInfo(name, email, provider));

      // 3. 추가 정보 입력이 필요하다는 의미로, 임시 권한을 가진 OAuth2User 반환
      return new DefaultOAuth2User(
          Collections.singleton(new SimpleGrantedAuthority("ROLE_GUEST")),
          attributes,
          "email"
      );
    }
  }
}