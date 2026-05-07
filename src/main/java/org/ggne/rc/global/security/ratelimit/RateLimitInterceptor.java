package org.ggne.rc.global.security.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitService rateLimitService;

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {

        // 컨트롤러 메서드가 아닌 요청은 그냥 통과
        if (!(handler instanceof HandlerMethod method)) return true;

        RateLimited annotation = method.getMethodAnnotation(RateLimited.class);
        if (annotation == null) return true;

        String key = resolveKey(request, annotation);
        Bucket bucket = rateLimitService.resolveBucket(key, annotation.capacity(), Duration.ofSeconds(annotation.refillSeconds()));
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // 남은 토큰 수를 응답 헤더에 포함 - 클라이언트가 현재 여유량 파악 가능.
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            return true;
        }

        // 토큰 소진 -> 429 반환
        long waitSeconds = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.addHeader("X-RateLimit-Retry-After", String.valueOf(waitSeconds));
        response.getWriter().write("Rate limit exceeded. Retry after " + waitSeconds + " seconds.");

        return false;
    }

    private String resolveKey(HttpServletRequest request, RateLimited annotation) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        // 인증 된 사용자 (userId가 Long인 경우)는 userId 기준으로 키 생성
        if (annotation.scope() == RateLimitScope.USER
                && auth != null
                && auth.getPrincipal() instanceof Long userId) {

            return "rl:user:" + userId + ":" + annotation.name();
        }

        // 비인증 요청은 IP 기준 
        return "rl:ip:" + getClientIp(request) + ":" + annotation.name();

    }

    private String getClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");

        if (forwarded != null && !forwarded.isBlank()) {
            // 프록시 체인에서 첫 번째 IP가 실제 클라이언트 IP
            return forwarded.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }
}
