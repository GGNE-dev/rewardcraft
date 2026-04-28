package org.ggne.rc.domain.user.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.participation.controller.ParticipationController.ParticipationResponse;
import org.ggne.rc.domain.participation.service.ParticipationService;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.service.UserService;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ParticipationService participationService;

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

    @GetMapping("/{id}/participations")
    public ApiResponse<List<ParticipationResponse>> getParticipations(@PathVariable Long id) {
        return ApiResponse.ok(participationService.findByUserId(id).stream()
                .map(ParticipationResponse::from).toList());
    }

    // DTO를 Controller 내부 record로 선언 — 이 API에서만 쓰이는 DTO는 별도 파일로 분리할 필요 없음
    public record UserResponse(Long id, String email, String nickname, String role) {
        public static UserResponse from(User user) {
            return new UserResponse(user.getId(), user.getEmail(),
                    user.getNickname(), user.getRole().name());
        }
    }

    public record NicknameUpdateRequest(@NotBlank String nickname) {}
}
