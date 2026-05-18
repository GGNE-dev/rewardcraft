package org.ggne.rc.global.config;

import io.github.bucket4j.distributed.ExpirationAfterWriteStrategy;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import io.github.bucket4j.redis.lettuce.Bucket4jLettuce;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.ByteArrayCodec;
import io.lettuce.core.codec.RedisCodec;
import io.lettuce.core.codec.StringCodec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class RateLimitConfig {

    @Value("${spring.data.redis.host}")
    private String redisHost;

    @Value("${spring.data.redis.port}")
    private int redisPort;

    // Bucket4j 전용 Lettuce 클라이언트 — Spring의 StringRedisTemplate과 별개로 생성
    // destroyMethod: 앱 종료 시 Lettuce 클라이언트 연결을 정상적으로 닫음
    @Bean(destroyMethod = "shutdown")
    public RedisClient rateLimitRedisClient() {
        return RedisClient.create("redis://" + redisHost + ":" + redisPort);
    }

    // ProxyManager: 키(문자열)별로 Redis에 저장된 버킷을 관리하는 Bucket4j 핵심 객체
    // key = "rl:user:42:mission-complete" 처럼 사용자+API 조합으로 버킷을 구분
    @Bean
    public ProxyManager<String> bucketProxyManager(RedisClient rateLimitRedisClient) {
        StatefulRedisConnection<String, byte[]> connection = rateLimitRedisClient.connect(
                RedisCodec.of(StringCodec.UTF8, ByteArrayCodec.INSTANCE));

        return Bucket4jLettuce.casBasedBuilder(connection)
                // 버킷이 꽉 찰 때까지 걸리는 시간(10분)이 지나면 Redis에서 자동 삭제
                // 비활성 사용자의 버킷이 Redis에 무한정 쌓이는 것을 방지
                .expirationAfterWrite(
                        ExpirationAfterWriteStrategy.basedOnTimeForRefillingBucketUpToMax(Duration.ofMinutes(10)))
                .build();
    }
}
