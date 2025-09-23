package com.workout.global.config;

import com.workout.auth.service.CustomOAuth2UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
    return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
      SecurityContextRepository securityContextRepository,
      CustomOAuth2UserService customOAuth2UserService,CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler) throws Exception { // 파라미터 추가
    http
        .csrf(csrf -> csrf.disable())
        .formLogin(form -> form.disable())
        //로그아웃 설정
        .logout(logout -> logout
            .logoutUrl("/api/auth/signout") // 로그아웃을 처리할 URL 지정
            .logoutSuccessHandler((request, response, authentication) -> {
              // 로그아웃 성공 시, 200 OK 상태와 성공 메시지를 응답
              response.setStatus(HttpServletResponse.SC_OK);
              response.getWriter().write("Logout Successful");
            })
            .deleteCookies("SESSION")    // 응답에 SESSION 쿠키를 삭제하라고 명시
            .invalidateHttpSession(true) // 세션을 무효화하여 Redis 데이터 삭제
        )
        // SecurityContext를 명시적으로 저장하고 로드할 때 사용할 Repository를 지정
        .securityContext(context -> context.securityContextRepository(securityContextRepository))

        .authorizeHttpRequests(auth -> auth
            .requestMatchers(
                "/actuator/**",
                "/api/auth/**",
                "/api/sync/**",
                "/v3/api-docs/**",       // OpenAPI 3.0 스펙 JSON/YAML 파일
                "/swagger-ui.html",      // Swagger UI 메인 HTML 페이지
                "/swagger-ui/**")
            .permitAll()
            .anyRequest().authenticated()
        )

        .exceptionHandling(e -> e
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
        )
        .oauth2Login(oauth2 -> oauth2
                .userInfoEndpoint(userInfo -> userInfo
                        .userService(customOAuth2UserService)
                )
            .successHandler(customAuthenticationSuccessHandler)
        );

    return http.build();
    }
}