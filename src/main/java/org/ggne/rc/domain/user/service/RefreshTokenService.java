package org.ggne.rc.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final StringRedisTemplate redisTemplate;

    // "refresh:42" 처럼 userId를 키로 사용
    private static final String KEY_PREFIX = "refresh:";

    public void save(Long userId, String refreshToken, Duration ttl) {
        redisTemplate.opsForValue().set(KEY_PREFIX + userId, refreshToken, ttl);
    }

    public String get(Long userId) {
        return redisTemplate.opsForValue().get(KEY_PREFIX + userId);
    }

    public void delete(Long userId) {
        redisTemplate.delete(KEY_PREFIX + userId);
    }

    // 저장된 토큰과 클라이언트가 보내온 토큰이 일치하는지 검증
    public boolean isValid(Long userId, String refreshToken) {
        String stored = this.get(userId);

        return stored != null && stored.equals(refreshToken);
    }
}
