package org.ggne.challenge.domain.challenge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class RankingService {

    private final RedisTemplate<String, Object> redisTemplate;
    private static final String RANKING_KEY = "ranking:challenge:%d";

    public void addPoints(Long challengeId, Long userId, long points) {
        String key = String.format(RANKING_KEY, challengeId);
        redisTemplate.opsForZSet().incrementScore(key, userId.toString(), points);
    }

    public List<RankingEntry> getTopN(Long challengeId, int n) {
        String key = String.format(RANKING_KEY, challengeId);
        Set<ZSetOperations.TypedTuple<Object>> tuples =
                redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, n - 1);

        if (tuples == null) return List.of();

        AtomicInteger rank = new AtomicInteger(1);
        return tuples.stream()
                .map(t -> new RankingEntry(
                        rank.getAndIncrement(),
                        Long.parseLong(t.getValue().toString()),
                        t.getScore().longValue()))
                .toList();
    }

    public Long getMyRank(Long challengeId, Long userId) {
        String key = String.format(RANKING_KEY, challengeId);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());
        return rank != null ? rank + 1 : null;
    }

    public Double getMyScore(Long challengeId, Long userId) {
        String key = String.format(RANKING_KEY, challengeId);
        return redisTemplate.opsForZSet().score(key, userId.toString());
    }

    public void clearRanking(Long challengeId) {
        redisTemplate.delete(String.format(RANKING_KEY, challengeId));
    }

    public record RankingEntry(int rank, Long userId, long score) {}
}
