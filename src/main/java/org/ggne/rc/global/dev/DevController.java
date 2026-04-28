package org.ggne.rc.global.dev;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.entity.OAuthProvider;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.entity.UserRole;
import org.ggne.rc.domain.user.repository.UserRepository;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * TODO: Ch 02 OAuth2 소셜 로그인 완료 후 이 파일 전체 삭제
 * 개발/테스트 전용 — 운영 환경에서 절대 활성화되면 안 됨
 */
@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
public class DevController {

    private final UserRepository userRepository;

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserInfo>> createUser(
            @RequestBody @Valid CreateRequest req) {
        User user = User.builder()
                .email(req.email())
                .nickname(req.nickname())
                .provider(OAuthProvider.KAKAO)          // 임시값
                .providerUserId("dev-" + req.email())   // 임시값
                .role(UserRole.USER)
                .build();
        User saved = userRepository.save(user);
        return ResponseEntity.status(201)
                .body(ApiResponse.ok(new UserInfo(saved.getId(), saved.getEmail(), saved.getNickname())));
    }

    public record CreateRequest(@NotBlank String email, @NotBlank String nickname) {}
    public record UserInfo(Long id, String email, String nickname) {}
}
