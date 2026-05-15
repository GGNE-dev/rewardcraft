package org.ggne.rc.global.security.ratelimit;

public enum RateLimitScope {
    USER,           // 인증 된 사용자 기준 (userId)
    IP              // 비인증 요청 기준 (클라이언트 IP)
}
