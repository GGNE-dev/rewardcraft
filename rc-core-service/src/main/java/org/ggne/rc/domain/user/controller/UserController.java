package org.ggne.rc.domain.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.service.UserService;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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

    public record UserResponse(Long id, String email, String nickname, String role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(),
                    user.getNickname(), user.getRole().name());
        }
    }

    public record NicknameUpdateRequest(@NotBlank String nickname) {}
}
