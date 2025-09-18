package com.workout.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationPublisher {

  private final RedisTemplate<String, Object> redisTemplate;

  public void publish(String cacheName, Object cacheKey) {
    String message = cacheName + "::" + cacheKey.toString();
    log.info("Publishing cache invalidation message: {}", message);
    redisTemplate.convertAndSend(RedisConfig.CACHE_INVALIDATION_CHANNEL, message);
  }
}