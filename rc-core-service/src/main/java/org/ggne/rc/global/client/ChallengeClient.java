package org.ggne.rc.global.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// challenge-service의 내부 API를 호출하는 Feign 클라이언트
// fallbackFactory: challenge-service 장애 시 CircuitBreaker가 열리면 fallback을 실행
@FeignClient(
        name = "challenge-service",
        url = "${services.challenge.url}",
        fallbackFactory = ChallengeClientFallback.class
)
public interface ChallengeClient {

    @GetMapping("/internal/participants/points")
    Long getUserTotalPoints(@RequestParam("userId") Long userId);

    @PostMapping("/internal/participants/points/deduct")
    void deductPoints(@RequestParam("userId") Long userId, @RequestParam("points") long points);
}
