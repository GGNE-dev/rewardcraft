package org.ggne.rc.domain.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.service.UserService;
import org.ggne.rc.global.client.ChallengeClient;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ChallengeClient challengeClient;

    // 프론트엔드 OAuth2 콜백에서 토큰 저장 후 호출 — 로그인한 사용자 본인 정보 조회
    @GetMapping("/me")
    public ApiResponse<UserResponse> getMe(@AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(UserResponse.from(userService.findById(userId)));
    }

    // 리워드 교환 페이지에서 보유 포인트 표시용 — challenge-service Feign 호출
    @GetMapping("/me/points")
    public ApiResponse<PointsResponse> getMyPoints(@AuthenticationPrincipal Long userId) {
        Long totalPoints = challengeClient.getUserTotalPoints(userId);
        return ApiResponse.ok(new PointsResponse(totalPoints != null ? totalPoints : 0L));
    }

    @GetMapping("/{id}")
    public ApiResponse<UserResponse> getUser(@PathVariable Long id) {
        return ApiResponse.ok(UserResponse.from(userService.findById(id)));
    }

    @PatchMapping("/{id}/nickname")
    public ApiResponse<UserResponse> updateNickname(
            @PathVariable Long id,
            @RequestBody @Valid NicknameUpdateRequest request) {
        return ApiResponse.ok(UserResponse.from(userService.updateNickname(id, request.nickname())));
    }

    // 사용자별 참여 챌린지 목록은 Challenge BC 소유 → challenge-service GET /api/participations?userId={id} 호출

    public record UserResponse(Long id, String email, String nickname, String role, String provider, boolean banned, String createdAt) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(),
                    user.getNickname(), user.getRole().name(), user.getProvider().name(),
                    user.isBanned(), user.getCreatedAt() != null ? user.getCreatedAt().toString() : null);
        }
    }

    public record NicknameUpdateRequest(@NotBlank String nickname) {}

    public record PointsResponse(long totalPoints) {}
}
