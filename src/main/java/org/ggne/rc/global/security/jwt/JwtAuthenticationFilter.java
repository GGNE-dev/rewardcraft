package org.ggne.rc.global.security.jwt;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.domain.user.entity.UserRole;
import org.springframework.http.HttpHeaders;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;


@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {

        String token = resolveToken(request);

        if (token != null && jwtProvider.validate(token)) {
            try {
                Claims claims = jwtProvider.parse(token);

                // Refresh Token으로 일반 API 호출하는 것을 차단
                if (!"ACCESS".equals(claims.get("type"))) {
                    chain.doFilter(request, response);
                    return;
                }

                Long userId = Long.parseLong(claims.getSubject());
                String role = (String) claims.get("role");
                UserRole userRole = UserRole.valueOf(role);

                // ROLE_X 하나 + 해당 역할의 모든 PERM_X를 합쳐 GrantedAuthority 목록 생성
                // DB 조회 없이 JWT 클레임 + UserRole 열거형만으로 권한 목록 완성 — Stateless 핵심
                List<GrantedAuthority> authorities = Stream.concat(
                        Stream.<GrantedAuthority>of(new SimpleGrantedAuthority("ROLE_" + role)),
                        userRole.getPermissions().stream()
                                .map(p -> new SimpleGrantedAuthority(p.name()))
                ).toList();

                // DB 조회 없이 JWT 클레임만으로 인증 객체 생성 — Stateless 핵심
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userId,   // principal — 컨트롤러에서 @AuthenticationPrincipal Long userId 로 꺼냄
                                null,
                                authorities
                        );


                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (Exception e) {
                log.warn("JWT authentication failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }

        // 토큰이 없거나 유효하지 않아도 반드시 다음 필터로 진행
        // 요청 차단은 이 필터의 역할이 아님 — AuthorizationFilter가 401/403 결정
        chain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);  // "Bearer " 이후 토큰 문자열만 추출
        }

        return null;
    }
}
