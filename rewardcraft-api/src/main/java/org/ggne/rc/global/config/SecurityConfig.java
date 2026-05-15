package org.ggne.rc.global.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.global.security.jwt.JwtAuthenticationFilter;
import org.ggne.rc.global.security.oauth2.CustomOAuth2UserService;
import org.ggne.rc.global.security.oauth2.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final OAuth2SuccessHandler oAuth2SuccessHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API + JWT 환경에서는 CSRF 불필요 (Authorization 헤더는 브라우저가 자동 전송 안 함)
                .csrf(AbstractHttpConfigurer::disable)
                // 세션을 만들지 않음 — JWT가 상태를 담당
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // 폼 로그인, HTTP Basic 인증 비활성
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                // 인증 실패 시 로그인 페이지로 리다이렉트하지 않고 401 반환 (기본 값인 LoginUrlAuthenticationEntryPoint 덮어씀)
                .exceptionHandling(e -> e.authenticationEntryPoint(
                        ((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "UnAuthorized")))
                )
                // 인가 규칙
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/error", "/login/**", "/oauth2/**", "/api/auth/**", "/api/dev/**").permitAll()
                        // /api/admin/** 의 세부 권한은 각 컨트롤러의 @PreAuthorize가 담당
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                // OAuth2 로그인 — 카카오/구글 연동
                .oauth2Login(oauth2 -> oauth2
                        .userInfoEndpoint(u -> u.userService(customOAuth2UserService))
                        .successHandler(oAuth2SuccessHandler)
                )
                // JWT 필터를 폼 로그인 필터 앞에 등록
                // 매 요청에서 JWT 검증이 먼저 끝나야 이후 인가 필터가 올바르게 동작함
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
