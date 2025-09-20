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
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
      feedIdsObj = zSetOps.rangeByScore(gymFeedsKey, firstFeedId + 1, Double.POSITIVE_INFINITY);
    } else {
      double maxScore = (lastFeedId != null) ? (double) lastFeedId : Double.POSITIVE_INFINITY;
      feedIdsObj = zSetOps.reverseRangeByScore(gymFeedsKey, Double.NEGATIVE_INFINITY, maxScore, 0,
          size + 1);
    }

    if (feedIdsObj == null || feedIdsObj.isEmpty()) {
      return getFeedsFromDBWithLock(gymId, lastFeedId, firstFeedId, size);
    }

    List<String> feedIds = feedIdsObj.stream()
        .map(obj -> String.valueOf(((Number) obj).longValue()))
        .collect(Collectors.toCollection(ArrayList::new));

    if (lastFeedId != null) {
      feedIds.remove(String.valueOf(lastFeedId));
    }

    if (feedIds.size() > size) {
      feedIds = feedIds.subList(0, size);
    }

    if (feedIds.isEmpty()) {
      return Collections.emptyList();
    }

    List<Object> cachedDetails = redisTemplate.opsForHash()
        .multiGet(FEEDS_DETAILS_KEY, Collections.unmodifiableCollection(feedIds));

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

    if (!missingFeedIds.isEmpty()) {
      List<Long> missingIds = missingFeedIds.stream().map(Long::valueOf).toList();
      List<Feed> fetchedFeeds = feedRepository.findAllByIdInOrderByIdDesc(missingIds);

      if (!fetchedFeeds.isEmpty()) {
        Map<String, FeedGridResponse> newCacheEntries = fetchedFeeds.stream()
            .collect(
                Collectors.toMap(feed -> String.valueOf(feed.getId()), FeedGridResponse::from));
        redisTemplate.opsForHash().putAll(FEEDS_DETAILS_KEY, newCacheEntries);
        fetchedFeeds.forEach(
            feed -> feedDetailsMap.put(String.valueOf(feed.getId()), FeedGridResponse.from(feed)));
      }
    }
    return feedIds.stream().map(feedDetailsMap::get).filter(Objects::nonNull).toList();
  }

  @Transactional(readOnly = true)
  public FeedSummaryResponse getFeedSummary(Long feedId) {
    final String feedIdStr = String.valueOf(feedId);

    // 1. N+1 방지를 위해 JOIN FETCH로 Feed와 Member를 한 번에 조회
    Feed feed = feedRepository.findByIdWithMember(feedId)
        .orElseThrow(() -> new RestApiException(FeedErrorCode.NOT_FOUND));

    // 2. Redis에서 카운터 조회 (DB 접근 없음)
    List<String> countKeys = List.of(
        FEED_LIKE_COUNT_KEY_PREFIX + feedIdStr,
        FEED_COMMENT_COUNT_KEY_PREFIX + feedIdStr
    );
    List<Object> counts = redisTemplate.opsForValue().multiGet(countKeys);
    Long likeCount =
        (counts != null && counts.get(0) != null) ? ((Number) counts.get(0)).longValue() : 0L;
    Long commentCount =
        (counts != null && counts.get(1) != null) ? ((Number) counts.get(1)).longValue() : 0L;

    return FeedSummaryResponse.of(feed, likeCount, commentCount);
  }

  private List<FeedGridResponse> getFeedsFromDBWithLock(Long gymId, Long lastFeedId,
      Long firstFeedId, int size) {
    final String lockKey = LOCK_GYM_FEEDS_KEY_PREFIX + gymId;
    RLock lock = redissonClient.getLock(lockKey);
    List<FeedGridResponse> result = Collections.emptyList();
    try {
      boolean isLocked = lock.tryLock(5, 10, TimeUnit.SECONDS);
      if (!isLocked) {
        log.warn("Failed to acquire lock for {}. Waiting and retrying from cache.", lockKey);
        Thread.sleep(200);
        return getFeedsForGrid(gymId, lastFeedId, firstFeedId, size);
      }
      try {
        if (redisTemplate.hasKey(GYM_FEEDS_KEY_PREFIX + gymId)) {
          return getFeedsForGrid(gymId, lastFeedId, firstFeedId, size);
        }

        List<Feed> feedsFromDB = feedRepository.findByGymIdFirstPage(gymId, PageRequest.of(0, 100));
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

  public List<FeedGridResponse> fallbackToDB(Long gymId, Long lastFeedId, Long firstFeedId,
      int size, Throwable t) {
    log.warn("Circuit breaker for Redis is OPEN. Falling back to DB for gymId: {}. Reason: {}",
        gymId, t.getMessage());
    List<Feed> feeds = fetchFeedsFromRepository(gymId, lastFeedId, firstFeedId, size);
    return feeds.stream().map(FeedGridResponse::from).toList();
  }

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

    Map<String, FeedGridResponse> feedDetailsMap = feeds.stream()
        .collect(Collectors.toMap(
            feed -> String.valueOf(feed.getId()),
            FeedGridResponse::from
        ));

    Set<ZSetOperations.TypedTuple<Object>> tuples = feeds.stream()
        .map(feed -> new DefaultTypedTuple<>(
            (Object) feed.getId(),
            (double) feed.getId()
        ))
        .collect(Collectors.toSet());

    redisTemplate.execute(new SessionCallback<>() {
      @Override
      public <K, V> List<Object> execute(RedisOperations<K, V> operations)
          throws DataAccessException {
        operations.multi();
        if (!feedDetailsMap.isEmpty()) {
          ((RedisTemplate<String, Object>) operations).opsForHash()
              .putAll(FEEDS_DETAILS_KEY, feedDetailsMap);
        }
        if (!tuples.isEmpty()) {
          ((RedisTemplate<String, Object>) operations).opsForZSet().add(gymFeedsKey, tuples);
        }
        return operations.exec();
      }
    });

    log.info("Populated L2 cache for gymId: {} with {} feeds.", gymId, feeds.size());
  }

  public void addFeedToCache(Feed feed) {
    final String gymFeedsKey = GYM_FEEDS_KEY_PREFIX + feed.getGym().getId();
    final String feedIdStr = String.valueOf(feed.getId());
    final FeedGridResponse response = FeedGridResponse.from(feed);

    redisTemplate.execute(new SessionCallback<>() {
      @Override
      public <K, V> List<Object> execute(RedisOperations<K, V> operations)
          throws DataAccessException {
        operations.multi();
        ((RedisTemplate<String, Object>) operations).opsForZSet()
            .add(gymFeedsKey, feed.getId(), (double) feed.getId());
        ((RedisTemplate<String, Object>) operations).opsForHash()
            .put(FEEDS_DETAILS_KEY, feedIdStr, response);
        return operations.exec();
      }
    });
  }

  public void removeFeedFromCache(Feed feed) {
    final String gymFeedsKey = GYM_FEEDS_KEY_PREFIX + feed.getGym().getId();
    final String feedIdStr = String.valueOf(feed.getId());

    redisTemplate.execute(new SessionCallback<>() {
      @Override
      public <K, V> List<Object> execute(RedisOperations<K, V> operations)
          throws DataAccessException {
        operations.multi();
        ((RedisTemplate<String, Object>) operations).opsForZSet().remove(gymFeedsKey, feedIdStr);
        ((RedisTemplate<String, Object>) operations).opsForHash()
            .delete(FEEDS_DETAILS_KEY, feedIdStr);
        ((RedisTemplate<String, Object>) operations).delete(List.of(
            FEED_LIKE_COUNT_KEY_PREFIX + feedIdStr,
            FEED_COMMENT_COUNT_KEY_PREFIX + feedIdStr
        ));
        return operations.exec();
      }
    });
  }
}