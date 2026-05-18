package org.ggne.rc.global.client;

import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.global.exception.ErrorCode;
import org.ggne.rc.global.exception.RCBusinessException;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

// challenge-service 장애 시 호출됨
// 포인트 조회 불가 → 리워드 교환도 불가 (0을 반환해 허용하는 것보다 명확한 에러가 안전)
@Slf4j
@Component
public class ChallengeClientFallback implements FallbackFactory<ChallengeClient> {

    @Override
    public ChallengeClient create(Throwable cause) {
        return userId -> {
            log.error("challenge-service 호출 실패 (userId={}): {}", userId, cause.getMessage());
            throw new RCBusinessException(ErrorCode.CHALLENGE_SERVICE_UNAVAILABLE);
        };
    }
}
