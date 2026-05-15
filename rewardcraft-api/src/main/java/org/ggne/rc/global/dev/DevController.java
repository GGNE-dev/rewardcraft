package org.ggne.rc.global.dev;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.entity.OAuthProvider;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.entity.UserRole;
import org.ggne.rc.domain.user.repository.UserRepository;
import org.ggne.rc.domain.user.service.UserService;
import org.ggne.rc.global.response.ApiResponse;
import org.ggne.rc.global.security.jwt.JwtProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

// local 프로파일에서만 Bean 등록 — 운영 환경 노출 원천 차단
@Profile("local")
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final JwtProvider jwtProvider;

    // 테스트 유저 생성
    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserInfo>> createUser(
            @RequestBody @Valid CreateRequest req) {
        User user = User.builder()
                .email(req.email())
                .nickname(req.nickname())
                .provider(OAuthProvider.KAKAO)
                .providerUserId("dev-" + req.email())
                .role(UserRole.USER)
                .build();
        User saved = userRepository.save(user);
        return ResponseEntity.status(201)
                .body(ApiResponse.ok(new UserInfo(saved.getId(), saved.getEmail(), saved.getNickname())));
    }

    // userId로 Access Token 즉시 발급 — OAuth2 흐름 없이 테스트용 토큰 획득
    // 실제 OAuth2 로그인과 동일한 JWT를 발급하므로 모든 인증/인가 로직이 그대로 동작함
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<Map<String, String>>> issueToken(@RequestParam Long userId) {
        User user = userService.findById(userId);
        String accessToken = jwtProvider.createAccessToken(userId, user.getRole());
        return ResponseEntity.ok(ApiResponse.ok(Map.of("accessToken", accessToken)));
    }

    public record CreateRequest(@NotBlank String email, @NotBlank String nickname) {}
    public record UserInfo(Long id, String email, String nickname) {}
}
