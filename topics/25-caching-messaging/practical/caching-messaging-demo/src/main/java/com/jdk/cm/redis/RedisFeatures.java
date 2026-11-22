package com.jdk.cm.redis;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

/** Typical Redis use cases beyond caching, with Spring Data Redis. */
@Service
public class RedisFeatures {

    private final StringRedisTemplate redis;

    public RedisFeatures(StringRedisTemplate redis) {
        this.redis = redis;
    }

    /** Key with a time-to-live: sessions, one-time codes, short-lived caches. */
    public void storeOtp(String user, String code, Duration ttl) {
        redis.opsForValue().set("otp:" + user, code, ttl);
    }

    public String otp(String user) {
        return redis.opsForValue().get("otp:" + user);
    }

    public Long ttlSeconds(String user) {
        return redis.getExpire("otp:" + user);
    }

    /** Fixed-window rate limiter: atomic INCR + EXPIRE, shared by every app instance. */
    public boolean allowRequest(String clientId, int limitPerMinute) {
        String key = "rate:" + clientId + ":" + (System.currentTimeMillis() / 60_000);
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1) {
            redis.expire(key, Duration.ofMinutes(1));
        }
        return count != null && count <= limitPerMinute;
    }

    /** Sorted set: a leaderboard ordered by score. */
    public void addScore(String player, double points) {
        redis.opsForZSet().incrementScore("leaderboard", player, points);
    }

    public List<String> topPlayers(int n) {
        Set<TypedTuple<String>> top = redis.opsForZSet().reverseRangeWithScores("leaderboard", 0, n - 1);
        return top == null ? List.of() : top.stream().map(t -> t.getValue() + "=" + t.getScore().intValue()).toList();
    }
}
