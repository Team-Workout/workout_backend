package com.workout.feed.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workout.feed.domain.Feed;
import com.workout.feed.dto.FeedGridResponse;
import com.workout.feed.dto.FeedSummaryResponse;
import com.workout.feed.repository.FeedRepository;
import com.workout.global.exception.RestApiException;
import com.workout.global.exception.errorcode.FeedErrorCode;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.zset.DefaultTuple;
import org.springframework.data.redis.connection.zset.Tuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FeedCacheService {

  private static final String GYM_FEEDS_KEY_PREFIX = "gym:feeds:";
  private static final String FEEDS_DETAILS_KEY = "feeds:details";
  private static final String LOCK_GYM_FEEDS_KEY_PREFIX = "lock:gym:feeds:";
  private static final String FEED_LIKE_COUNT_KEY_PREFIX = "counts:like:feed:";
  private static final String FEED_COMMENT_COUNT_KEY_PREFIX = "counts:comment:feed:";

  private final FeedRepository feedRepository;
  private final RedisTemplate<String, Object> redisTemplate;
  private final RedissonClient redissonClient;
  private final ObjectMapper objectMapper;

  @CircuitBreaker(name = "redis-circuit", fallbackMethod = "fallbackToDB")
  public List<FeedGridResponse> getFeedsForGrid(Long gymId, Long lastFeedId, Long firstFeedId,
      int size) {
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
      feedIdsObj = zSetOps.reverseRangeByScore(gymFeedsKey, Double.NEGATIVE_INFINITY, maxScore, 0,
          size + 1);
    }

    if (feedIdsObj == null || feedIdsObj.isEmpty()) {
      // If the cache is empty, try fetching from the DB with a distributed lock
      return getFeedsFromDBWithLock(gymId, lastFeedId, firstFeedId, size);
    }

    List<String> feedIds = new ArrayList<>(feedIdsObj.stream().map(String::valueOf).toList());

    // Remove the cursor ID from the list if it exists
    if (lastFeedId != null) {
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
    List<Object> cachedDetails = redisTemplate.opsForHash()
        .multiGet(FEEDS_DETAILS_KEY, Collections.unmodifiableCollection(feedIds));

    // Cache-Aside Pattern: Find which feeds are missing from the cache
    Map<String, FeedGridResponse> feedDetailsMap = new HashMap<>();
    List<String> missingFeedIds = new ArrayList<>();
    for (int i = 0; i < feedIds.size(); i++) {
      if (cachedDetails.get(i) != null) {
        feedDetailsMap.put(feedIds.get(i),
            objectMapper.convertValue(cachedDetails.get(i), FeedGridResponse.class));
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
            .collect(
                Collectors.toMap(feed -> String.valueOf(feed.getId()), FeedGridResponse::from));
        redisTemplate.opsForHash().putAll(FEEDS_DETAILS_KEY, newCacheEntries);
        fetchedFeeds.forEach(
            feed -> feedDetailsMap.put(String.valueOf(feed.getId()), FeedGridResponse.from(feed)));
      }
    }

    // Return the final list in the correct order
    return feedIds.stream().map(feedDetailsMap::get).filter(Objects::nonNull).toList();
  }

  public FeedSummaryResponse getFeedSummary(Long feedId) {
    final String feedIdStr = String.valueOf(feedId);

    // 1. Redis HASH에서 피드의 정적 데이터를 먼저 조회합니다.
    Object cachedDetail = redisTemplate.opsForHash().get(FEEDS_DETAILS_KEY, feedIdStr);
    FeedGridResponse gridResponse;

    // 2. 캐시 미스(Cache Miss) 처리: HASH에 데이터가 없으면 DB에서 조회 후 캐시에 저장합니다.
    if (cachedDetail == null) {
      Feed feed = feedRepository.findById(feedId)
          .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));
      gridResponse = FeedGridResponse.from(feed);
      // 다음 조회를 위해 HASH 캐시를 채웁니다.
      redisTemplate.opsForHash().put(FEEDS_DETAILS_KEY, feedIdStr, gridResponse);
    } else {
      // 캐시 히트(Cache Hit): 가져온 데이터를 FeedGridResponse 객체로 변환합니다.
      gridResponse = objectMapper.convertValue(cachedDetail, FeedGridResponse.class);
    }

    // FeedGridResponse에서 FeedSummaryContent를 생성합니다.
    FeedSummaryResponse.FeedSummaryContent feedSummaryContent = new FeedSummaryResponse.FeedSummaryContent(
        gridResponse.feedId(),
        gridResponse.imageUrl(),
        gridResponse.authorUsername(),
        gridResponse.authorProfileImageUrl()
    );

    // 3. Redis에서 좋아요/댓글 카운터를 한 번의 요청(MGET)으로 가져옵니다.
    List<String> countKeys = List.of(
        FEED_LIKE_COUNT_KEY_PREFIX + feedIdStr,
        FEED_COMMENT_COUNT_KEY_PREFIX + feedIdStr
    );
    List<Object> counts = redisTemplate.opsForValue().multiGet(countKeys);

    // 4. 카운터 값을 파싱합니다. 키가 존재하지 않으면(null) 0으로 처리합니다.
    Long likeCount = 0L;
    if (counts != null && counts.get(0) != null) {
      // Redis의 카운터는 숫자로 저장되므로 Number 타입으로 안전하게 변환합니다.
      likeCount = ((Number) counts.get(0)).longValue();
    }

    Long commentCount = 0L;
    if (counts != null && counts.get(1) != null) {
      commentCount = ((Number) counts.get(1)).longValue();
    }

    // 5. 정적 데이터와 동적 카운터 데이터를 조합하여 최종 응답을 생성합니다.
    return FeedSummaryResponse.of(feedSummaryContent, likeCount, commentCount);
  }

  private List<FeedGridResponse> getFeedsFromDBWithLock(Long gymId, Long lastFeedId,
      Long firstFeedId, int size) {
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
        if (redisTemplate.hasKey(GYM_FEEDS_KEY_PREFIX + gymId)) {
          return getFeedsForGrid(gymId, lastFeedId, firstFeedId, size);
        }

        // Fetch initial data from DB
        List<Feed> feedsFromDB = feedRepository.findByGymIdFirstPage(gymId,
            PageRequest.of(0, 100)); // Load initial 100 feeds
        if (!feedsFromDB.isEmpty()) {
          populateCache(gymId, feedsFromDB);
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
  public List<FeedGridResponse> fallbackToDB(Long gymId, Long lastFeedId, Long firstFeedId,
      int size, Throwable t) {
    log.warn("Circuit breaker for Redis is OPEN. Falling back to DB for gymId: {}. Reason: {}",
        gymId, t.getMessage());
    // Use the correct repository methods
    List<Feed> feeds = fetchFeedsFromRepository(gymId, lastFeedId, firstFeedId, size);
    return feeds.stream().map(FeedGridResponse::from).toList();
  }

  // Helper method to fetch feeds from the repository based on cursor
  private List<Feed> fetchFeedsFromRepository(Long gymId, Long lastFeedId, Long firstFeedId,
      int size) {
    Pageable pageable = PageRequest.of(0, size);
    if (firstFeedId != null) {
      return feedRepository.findNewerFeedsByGymIdWithCursor(gymId, firstFeedId);
    } else if (lastFeedId != null) {
      return feedRepository.findOlderFeedsByGymIdWithCursor(gymId, lastFeedId, pageable);
    } else {
      return feedRepository.findByGymIdFirstPage(gymId, pageable);
    }
  }

  private void populateCache(Long gymId, List<Feed> feeds) {
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