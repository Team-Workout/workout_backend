// src/main/java/com/workout/feed/service/FeedCacheService.java
package com.workout.feed.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.feed.domain.Feed;
import com.workout.feed.dto.FeedGridResponse;
import com.workout.feed.repository.FeedRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.zset.DefaultTuple;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.serializer.RedisSerializer;
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedCacheService {
  private static final String GYM_FEEDS_KEY_PREFIX = "gym:feeds:";
  private static final String FEEDS_DETAILS_KEY = "feeds:details";
  private static final String LOCK_GYM_FEEDS_KEY_PREFIX = "lock:gym:feeds:";

  private final FeedRepository feedRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedissonClient redissonClient;
  private final ObjectMapper objectMapper;

  @CircuitBreaker(name = "redis-circuit", fallbackMethod = "fallbackToDB")
  public List<FeedGridResponse> getFeedsForGrid(Long gymId, Long lastFeedId, Long firstFeedId, int size) {
    final String gymFeedsKey = GYM_FEEDS_KEY_PREFIX + gymId;
    ZSetOperations<String, Object> zSetOps = redisTemplate.opsForZSet();

    Set<Object> feedIdsObj;
    if (firstFeedId != null) {
      // Pull-to-refresh: Get newer feeds
      feedIdsObj = zSetOps.rangeByScore(gymFeedsKey, firstFeedId + 1, Double.POSITIVE_INFINITY);
    } else {
      // Infinite scroll: Get older feeds
      double maxScore = (lastFeedId != null) ? (double) lastFeedId : Double.POSITIVE_INFINITY;
      // Get one extra item to check for the next page
      feedIdsObj = zSetOps.reverseRangeByScore(gymFeedsKey, Double.NEGATIVE_INFINITY, maxScore, 0, size + 1);
    }

    if (feedIdsObj == null || feedIdsObj.isEmpty()) {
      // If the cache is empty, try fetching from the DB with a distributed lock
      return getFeedsFromDBWithLock(gymId, lastFeedId, firstFeedId, size);
    }

    List<String> feedIds = new ArrayList<>(feedIdsObj.stream().map(String::valueOf).toList());

    // Remove the cursor ID from the list if it exists
    if(lastFeedId != null){
      feedIds.remove(String.valueOf(lastFeedId));
    }

    // Limit the results to the requested size
    if (feedIds.size() > size) {
      feedIds = feedIds.subList(0, size);
    }

    if (feedIds.isEmpty()) {
      return Collections.emptyList();
    }

    // Use HASH to get all feed details in one command
    List<Object> cachedDetails = redisTemplate.opsForHash().multiGet(FEEDS_DETAILS_KEY, Collections.unmodifiableCollection(feedIds));

    // Cache-Aside Pattern: Find which feeds are missing from the cache
    Map<String, FeedGridResponse> feedDetailsMap = new HashMap<>();
    List<String> missingFeedIds = new ArrayList<>();
    for (int i = 0; i < feedIds.size(); i++) {
      if (cachedDetails.get(i) != null) {
        feedDetailsMap.put(feedIds.get(i), objectMapper.convertValue(cachedDetails.get(i), FeedGridResponse.class));
      } else {
        missingFeedIds.add(feedIds.get(i));
      }
    }

    // If there are missing feeds, fetch them from the DB
    if (!missingFeedIds.isEmpty()) {
      List<Long> missingIds = missingFeedIds.stream().map(Long::valueOf).toList();
      List<Feed> fetchedFeeds = feedRepository.findAllByIdInOrderByIdDesc(missingIds);

      // Populate the cache with the newly fetched feeds
      if (!fetchedFeeds.isEmpty()) {
        Map<Object, Object> newCacheEntries = fetchedFeeds.stream()
            .collect(Collectors.toMap(feed -> String.valueOf(feed.getId()), FeedGridResponse::from));
        redisTemplate.opsForHash().putAll(FEEDS_DETAILS_KEY, newCacheEntries);
        fetchedFeeds.forEach(feed -> feedDetailsMap.put(String.valueOf(feed.getId()), FeedGridResponse.from(feed)));
      }
    }

    // Return the final list in the correct order
    return feedIds.stream().map(feedDetailsMap::get).filter(Objects::nonNull).toList();
  }

  private List<FeedGridResponse> getFeedsFromDBWithLock(Long gymId, Long lastFeedId, Long firstFeedId, int size) {
    final String lockKey = LOCK_GYM_FEEDS_KEY_PREFIX + gymId;
    RLock lock = redissonClient.getLock(lockKey);
    List<FeedGridResponse> result = Collections.emptyList();
    try {
      // Try to acquire the lock for 5 seconds, and automatically release it after 10 seconds
      boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
      if (!isLocked) {
        log.warn("Failed to acquire lock for {}. Waiting and retrying from cache.", lockKey);
        Thread.sleep(200);
        return getFeedsForGrid(gymId, lastFeedId, firstFeedId, size);
      }

      try {
        // Double-checked locking: Check the cache again after acquiring the lock
        if (Boolean.TRUE.equals(redisTemplate.hasKey(GYM_FEEDS_KEY_PREFIX + gymId))) {
          return getFeedsForGrid(gymId, lastFeedId, firstFeedId, size);
        }

        // Fetch initial data from DB
        List<Feed> feedsFromDB = feedRepository.findByGymIdFirstPage(gymId, PageRequest.of(0, 100)); // Load initial 100 feeds
        if (!feedsFromDB.isEmpty()) {
          populateL2Cache(gymId, feedsFromDB);
          result = feedsFromDB.stream().limit(size).map(FeedGridResponse::from).toList();
        }
      } finally {
        if (lock.isHeldByCurrentThread()) {
          lock.unlock();
        }
      }
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Thread interrupted while waiting for lock.", e);
    }
    return result;
  }

  // Fallback method for the circuit breaker
  public List<FeedGridResponse> fallbackToDB(Long gymId, Long lastFeedId, Long firstFeedId, int size, Throwable t) {
    log.warn("Circuit breaker for Redis is OPEN. Falling back to DB for gymId: {}. Reason: {}", gymId, t.getMessage());
    // Use the correct repository methods
    List<Feed> feeds = fetchFeedsFromRepository(gymId, lastFeedId, firstFeedId, size);
    return feeds.stream().map(FeedGridResponse::from).toList();
  }

  // Helper method to fetch feeds from the repository based on cursor
  private List<Feed> fetchFeedsFromRepository(Long gymId, Long lastFeedId, Long firstFeedId, int size) {
    Pageable pageable = PageRequest.of(0, size);
    if (firstFeedId != null) {
      return feedRepository.findNewerFeedsByGymIdWithCursor(gymId, firstFeedId);
    } else if (lastFeedId != null) {
      return feedRepository.findOlderFeedsByGymIdWithCursor(gymId, lastFeedId, pageable);
    } else {
      return feedRepository.findByGymIdFirstPage(gymId, pageable);
    }
  }

  private void populateL2Cache(Long gymId, List<Feed> feeds) {
    final String gymFeedsKey = GYM_FEEDS_KEY_PREFIX + gymId;

    redisTemplate.executePipelined((RedisConnection connection) -> {
      final RedisSerializer<String> keySerializer = (RedisSerializer<String>) redisTemplate.getKeySerializer();
      final RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) redisTemplate.getValueSerializer();
      final RedisSerializer<String> hashKeySerializer = (RedisSerializer<String>) redisTemplate.getHashKeySerializer();

      // 1. HASH(HMSET) 명령 준비 (이전과 동일)
      final byte[] feedDetailsKeyBytes = keySerializer.serialize(FEEDS_DETAILS_KEY);
      final Map<byte[], byte[]> feedDetailsMap = feeds.stream()
          .collect(Collectors.toMap(
              feed -> hashKeySerializer.serialize(String.valueOf(feed.getId())),
              feed -> valueSerializer.serialize(FeedGridResponse.from(feed))
          ));

      if (feedDetailsKeyBytes != null && !feedDetailsMap.isEmpty()) {
        connection.hashCommands().hMSet(feedDetailsKeyBytes, feedDetailsMap);
      }

      // 2. Sorted Set(ZADD) 명령 준비
      final byte[] gymFeedsKeyBytes = keySerializer.serialize(gymFeedsKey);

      // 🔥 [최종 수정] new DefaultTypedTuple -> new DefaultTuple로 변경
      final Set<Tuple> tuples = feeds.stream()
          .map(feed -> new DefaultTuple(
              valueSerializer.serialize(String.valueOf(feed.getId())), // 값(value)
              (double) feed.getId() // 점수(score)
          ))
          .collect(Collectors.toSet());

      if (gymFeedsKeyBytes != null && !tuples.isEmpty()) {
        connection.zSetCommands().zAdd(gymFeedsKeyBytes, tuples);
      }

      return null;
    });

    log.info("Populated L2 cache for gymId: {} with {} feeds using pipeline.", gymId, feeds.size());
  }


  public void addFeedToCache(Feed feed) {
    final String gymFeedsKey = GYM_FEEDS_KEY_PREFIX + feed.getGym().getId();
    final String feedIdStr = String.valueOf(feed.getId());
    final FeedGridResponse response = FeedGridResponse.from(feed);

    redisTemplate.executePipelined((RedisConnection connection) -> {
      redisTemplate.opsForZSet().add(gymFeedsKey, feedIdStr, (double) feed.getId());
      redisTemplate.opsForHash().put(FEEDS_DETAILS_KEY, feedIdStr, response);
      return null;
    });
  }

  public void removeFeedFromCache(Feed feed) {
    final String gymFeedsKey = GYM_FEEDS_KEY_PREFIX + feed.getGym().getId();
    final String feedIdStr = String.valueOf(feed.getId());

    // ZREM과 HDEL을 파이프라인으로 묶어 한번에 처리합니다.
    redisTemplate.executePipelined((RedisConnection connection) -> {
      redisTemplate.opsForZSet().remove(gymFeedsKey, feedIdStr);
      redisTemplate.opsForHash().delete(FEEDS_DETAILS_KEY, feedIdStr);
      return null;
    });
  }
}