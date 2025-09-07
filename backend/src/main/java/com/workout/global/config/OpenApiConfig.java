package com.workout.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

  @Bean
  public OpenAPI openAPI() {
    // 1. API 기본 정보 (제목, 설명, 버전)
    Info info = new Info()
        .title("Workout-Service API Documentation")
        .description("헬스케어 워크아웃 서비스의 API 명세서입니다.")
        .version("v0.0.1");

    // 2. 서버 URL 설정 (개발/운영 서버 URL을 명시)
    // 로컬 개발, Docker 개발, 실제 프로덕션 서버 URL을 모두 등록할 수 있습니다.
    Server localServer = new Server().url("http://localhost:8080").description("Local Development Server");
    Server devServer = new Server().url("https://dev.workout.com").description("Development Server (for Docker)");

    // 3. 인증 스키마 설정 (중요)
    // 우리가 만든 SecurityConfig는 세션 쿠키(SESSION) 기반 인증을 사용합니다.
    // Swagger UI가 요청을 보낼 때 이 쿠키를 포함하도록 설정해야 합니다.
    SecurityScheme cookieAuth = new SecurityScheme()
        .type(SecurityScheme.Type.APIKEY)
        .in(SecurityScheme.In.COOKIE)
        .name("SESSION");

    // 4. API에 글로벌 보안 요구사항 적용
    // 모든 API 엔드포인트에 "cookieAuth" (우리가 방금 정의한 세션 쿠키)를 요구하도록 설정합니다.
    //SecurityRequirement securityRequirement = new SecurityRequirement().addList("cookieAuth");

    return new OpenAPI()
        .info(info)
        .servers(List.of(localServer, devServer)) // 서버 목록 등록
        .components(new Components().addSecuritySchemes("cookieAuth", cookieAuth)); // 3번에서 만든 스키마 등록
        //.security(List.of(securityRequirement)); // 4번에서 만든 보안 요구사항 전역 적용
  }
}