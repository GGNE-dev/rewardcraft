package org.ggne.rc.global.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.domain.user.entity.UserRole;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;


@Slf4j
@Component
public class JwtProvider {

    private final SecretKey secretKey;
    private final long accessTokenValiditySeconds;
    private final long refreshTokenValiditySeconds;

    // @Value로 application.yaml의 jwt.* 값을 주입받아 생성자에서 SecretKey를 딱 1번만 빌드
    // secretKey 빌드는 비용이 있으므로 매 요청마다 하면 안 된다
    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-validity}") long accessTokenValiditySeconds,
            @Value("${jwt.refresh-token-validity}") long refreshTokenValiditySeconds) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);  // HS256 키 최소 32바이트 요구
        this.accessTokenValiditySeconds = accessTokenValiditySeconds;
        this.refreshTokenValiditySeconds = refreshTokenValiditySeconds;
    }

    public String createAccessToken(Long userId, UserRole role) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role.name())
                // type 클레임: JwtAuthenticationFilter에서 Access를 Refresh 자리에 못 쓰게 막는 안전장치
                .claim("type", "ACCESS")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTokenValiditySeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    public String createRefreshToken(Long userId) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "REFRESH")
                // jti(JWT ID): Refresh Token마다 고유 식별자 부여
                // Ch 03 Refresh Token 회전(rotation) 구현 시, 재사용된 토큰 감지에 사용
                .id(UUID.randomUUID().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTokenValiditySeconds)))
                .signWith(secretKey, Jwts.SIG.HS256)
                .compact();
    }

    // 토큰 파싱 — 실패 시 jjwt 예외를 그대로 던진다. validate()나 필터 내부에서만 호출.
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // 외부(필터)에서 호출하는 검증 메서드 — 예외를 직접 처리하고 boolean 반환
    public boolean validate(String token) {
        try {
            parse(token);
            return true;
        } catch (ExpiredJwtException e) {
            // 만료는 정상적인 토큰 라이프사이클 — debug 레벨로 충분
            log.debug("JWT expired: {}", e.getMessage());
        } catch (UnsupportedJwtException | MalformedJwtException | SignatureException e) {
            // 구조가 잘못됐거나 서명이 불일치 — 위변조 의심, warn 레벨
            log.warn("JWT invalid: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT is empty or null");
        }

        return false;
    }
}
