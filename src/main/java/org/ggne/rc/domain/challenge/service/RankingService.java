package org.ggne.rc.domain.challenge.service;

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

    // 미션 완료 시 호출 — 기존 점수에 points를 더함 (없으면 신규 추가)
    public void addPoints(Long challengeId, Long userId, long points) {
        String key = String.format(RANKING_KEY, challengeId);

        redisTemplate.opsForZSet().incrementScore(key, userId.toString(), points);
    }

    // 상위 N명 조회 — score 내림차순
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

    // 내 순위 조회 — reverseRank는 0부터 시작하므로 +1
    public Long getMyRank(Long challengeId, Long userId) {
        String key = String.format(RANKING_KEY, challengeId);
        Long rank = redisTemplate.opsForZSet().reverseRank(key, userId.toString());

        return rank != null ? rank + 1 : null;
    }

    // 내 점수 조회
    public Double getMyScore(Long challengeId, Long userId) {
        String key = String.format(RANKING_KEY, challengeId);

        return redisTemplate.opsForZSet().score(key, userId.toString());
    }

    public record RankingEntry(int rank, Long userId, long score) {}
}
