package com.workout.global.config;


import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

@TestConfiguration
public class EmbeddedRedisConfig {

  private RedisServer redisServer;

  public EmbeddedRedisConfig() {
    // 사용 가능한 포트를 동적으로 찾아 Redis 서버를 생성합니다.
    this.redisServer = new RedisServer(6379); // 혹은 RedisServer.builder().port(6379).build();
  }

  @PostConstruct
  public void startRedis() {
    try {
      redisServer.start();
    } catch (Exception e) {
      // 이미 실행 중일 경우를 대비한 예외 처리
      System.err.println("Embedded Redis already running or failed to start: " + e.getMessage());
    }
  }

  @PreDestroy
  public void stopRedis() {
    if (redisServer != null) {
      redisServer.stop();
    }
  }
}