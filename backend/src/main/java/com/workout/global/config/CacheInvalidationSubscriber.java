package com.workout.global.config;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheInvalidationSubscriber {

  private final CacheManager cacheManager;

  public void handleMessage(String message) {
    try {
      log.info("Received cache invalidation message: {}", message);
      String[] parts = message.split("::", 2);
      if (parts.length == 2) {
        String cacheName = parts[0];
        String cacheKey = parts[1];
        cacheManager.getCache(cacheName).evict(cacheKey);
        log.info("Evicted L1 cache: {}::{}", cacheName, cacheKey);
      }
    } catch (Exception e) {
      log.error("Error while handling cache invalidation message: {}", message, e);
    }
  }
}