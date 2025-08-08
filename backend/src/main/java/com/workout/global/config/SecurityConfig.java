package com.workout.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. CSRF 보호 비활성화 (API 서버이므로)
                .csrf(csrf -> csrf.disable())
                // .2 로그인 로그아웃 비활성화
                .formLogin(form -> form.disable())
                .logout(logout -> logout.disable())

                // 3. 요청별 접근 권한 설정
                .authorizeHttpRequests(auth -> auth
                        // AuthController의 모든 경로는 인증 없이 접근 가능해야 함
                        .requestMatchers("/api/auth/**").permitAll()
                        // 그 외 나머지 모든 요청은 반드시 인증(로그인)을 거쳐야 함
                        .anyRequest().authenticated()
                )

                // 4. 인증되지 않은 사용자의 접근 처리
                // API 서버이므로 로그인 페이지로 리다이렉트하는 대신 401 Unauthorized 에러를 응답
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                );

        return http.build();
    }
}