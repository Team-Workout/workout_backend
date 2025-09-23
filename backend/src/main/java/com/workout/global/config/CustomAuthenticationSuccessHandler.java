package com.workout.global.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

  private final HttpSessionOAuth2AuthorizationRequestRepository authorizationRequestRepository;
  @Value("${spring.app.oauth2.authorized-redirect-uris}")
  private List<String> authorizedRedirectUris;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String targetUrl = determineTargetUrl(request);

    String finalUrl = buildRedirectUrl(targetUrl, authentication, request);

    response.sendRedirect(finalUrl);
  }

  protected String determineTargetUrl(HttpServletRequest request) {
    // 세션에서 OAuth2 인증 요청 정보를 가져옵니다.
    OAuth2AuthorizationRequest authorizationRequest = authorizationRequestRepository.loadAuthorizationRequest(
        request);

    if (authorizationRequest == null) {
      // 기본 fallback URL을 반환하거나 예외를 던질 수 있습니다.
      // 여기서는 허용된 URI 목록의 첫 번째를 기본값으로 사용합니다.
      return authorizedRedirectUris.get(0);
    }

    Optional<String> redirectUri = Optional.ofNullable(authorizationRequest.getRedirectUri());

    return redirectUri
        .filter(this::isAuthorizedRedirectUri)
        .orElseThrow(() -> new IllegalArgumentException("허용되지 않은 리디렉션 URI입니다."));
  }

  // URI가 허용 목록에 있는지 검증하는 메소드
  private boolean isAuthorizedRedirectUri(String uri) {
    URI clientRedirectUri = URI.create(uri);

    return authorizedRedirectUris.stream()
        .anyMatch(authorizedUri -> {
          URI authorizedURI = URI.create(authorizedUri);
          // http, https 프로토콜의 경우 host와 port를 모두 비교
          if (authorizedURI.getScheme().equalsIgnoreCase("http") ||
              authorizedURI.getScheme().equalsIgnoreCase("httpss")) {
            return authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost())
                && authorizedURI.getPort() == clientRedirectUri.getPort();
          }
          // 그 외의 경우 (커스텀 스킴 등) scheme과 host만 비교
          else {
            return authorizedURI.getScheme().equalsIgnoreCase(clientRedirectUri.getScheme())
                && authorizedURI.getHost().equalsIgnoreCase(clientRedirectUri.getHost());
          }
        });
  }

  private String buildRedirectUrl(String targetUrl, Authentication authentication,
      HttpServletRequest request) {
    UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString(targetUrl)
        .queryParam("sessionId", request.getSession().getId());

    if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_GUEST"))) {
      uriBuilder.queryParam("isNewUser", "true");
    } else {
      uriBuilder.queryParam("isNewUser", "false");
    }

    return uriBuilder.build().toUriString();
  }
}