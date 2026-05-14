package org.ggne.rc.domain.user.controller;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.service.RefreshTokenService;
import org.ggne.rc.domain.user.service.UserService;
import org.ggne.rc.global.exception.BusinessException;
import org.ggne.rc.global.exception.ErrorCode;
import org.ggne.rc.global.response.ApiResponse;
import org.ggne.rc.global.security.jwt.JwtProvider;
import org.ggne.rc.global.security.ratelimit.RateLimitScope;
import org.ggne.rc.global.security.ratelimit.RateLimited;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final UserService userService;

    @PostMapping("/refresh")
    @RateLimited(name = "auth-refresh", capacity = 3, refillSeconds = 60, scope = RateLimitScope.IP)
    public ApiResponse<Map<String, String>> refresh(@RequestParam String refreshToken) {

        // 1. JWT 자체가 유효한지 (서명, 만료) 먼저 검증
        if (! jwtProvider.validate(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // 2. REFRESH 타입인지 확인 — Access Token을 이 엔드포인트에 보내는 것을 차단
        Claims claims = jwtProvider.parse(refreshToken);
        if (!"REFRESH".equals(claims.get("type"))) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        Long userId = jwtProvider.getUserId(refreshToken);

        // 3. Redis에 저장된 토큰과 일치하는지 검증 (탈취 감지)
        if (! refreshTokenService.isValid(userId, refreshToken)) {
            // 불일치 = 이미 사용된 토큰 재사용 시도 → 강제 로그아웃
            refreshTokenService.delete(userId);
            throw new BusinessException(ErrorCode.TOKEN_REUSE_DETECTED);
        }

        // 4. DB에서 현재 role 조회 — Refresh Token에는 role이 없고, 발급 이후 role이 변경됐을 수 있음
        User user = userService.findById(userId);

        // 5. 새 토큰 발급 + Redis 갱신 (Rotation)
        String newAccessToken = jwtProvider.createAccessToken(userId, user.getRole());
        String newRefreshToken = jwtProvider.createRefreshToken(userId);
        refreshTokenService.save(userId, newRefreshToken, Duration.ofDays(14));

        return ApiResponse.ok(Map.of(
                "accessToken", newAccessToken,
                "refreshToken", newRefreshToken
        ));
    }
}
