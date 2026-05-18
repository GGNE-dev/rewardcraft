package org.ggne.rc.global.security.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RateLimitService {

    private final ProxyManager<String> bucketProxyManager;

    public Bucket resolveBucket(String key, long capacity, Duration refillPeriod) {
        BucketConfiguration config = BucketConfiguration.builder()
                // refillPeriod 마다 capacity만큼 한꺼번에 충전
                // ex. capacity=5, refillPeriod=1초 → 1초마다 토큰 5개 한 번에 채워짐
                .addLimit(limit -> limit.capacity(capacity).refillIntervally(capacity, refillPeriod))
                .build();

        return bucketProxyManager.builder().build(key, () -> config);
    }
}
