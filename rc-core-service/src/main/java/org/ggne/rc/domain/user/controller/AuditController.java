package org.ggne.rc.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.audit.entity.AuditLog;
import org.ggne.rc.domain.audit.repository.AuditLogRepository;
import org.ggne.rc.domain.reward.repository.RewardExchangeRepository;
import org.ggne.rc.domain.user.dto.UserSearchCondition;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.entity.UserRole;
import org.ggne.rc.domain.user.repository.UserRepository;
import org.ggne.rc.domain.user.service.UserService;
import org.ggne.rc.global.audit.Audited;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AuditController {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final RewardExchangeRepository rewardExchangeRepository;

    // OPERATOR, ADMIN 모두 접근 가능
    @GetMapping("/challenges")
    @PreAuthorize("hasAuthority('PERM_CHALLENGE_CREATE')")
    @Audited(action = "CHALLENGE_MANAGE_VIEW", targetType = "CHALLENGE", targetIdSpEL = "'all'")
    public ApiResponse<String> challengeManage() {
        return ApiResponse.ok("챌린지 관리 접근 성공");
    }

    // ADMIN만 접근 가능
    @GetMapping("/users/ban")
    @PreAuthorize("hasAuthority('PERM_USER_BAN')")
    @Audited(action = "USER_BAN_VIEW", targetType = "USER", targetIdSpEL = "'all'")
    public ApiResponse<String> userBan() {
        return ApiResponse.ok("사용자 정지 접근 성공");
    }

    // ADMIN만 접근 가능
    @GetMapping("/operators")
    @PreAuthorize("hasAuthority('PERM_OPERATOR_MANAGE')")
    @Audited(action = "OPERATOR_MANAGE_VIEW", targetType = "OPERATOR", targetIdSpEL = "'all'")
    public ApiResponse<String> operatorManage() {
        return ApiResponse.ok("운영자 관리 접근 성공");
    }

    // 감사 로그 조회 — ADMIN만
    @GetMapping("/audit-logs")
    @PreAuthorize("hasAuthority('PERM_AUDIT_READ')")
    public ApiResponse<List<AuditLog>> getAuditLogs() {
        return ApiResponse.ok(auditLogRepository.findAll());
    }

    // 회원 검색 (QueryDSL) — OPERATOR, ADMIN
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('PERM_CHALLENGE_CREATE')")
    public ApiResponse<PagedUserResponse> searchUsers(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String role,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        UserSearchCondition condition = new UserSearchCondition();
        condition.setEmail(email);
        condition.setNickname(null);
        if (role != null && !role.isBlank()) {
            condition.setRole(UserRole.valueOf(role));
        }

        Page<User> result = userRepository.search(condition, PageRequest.of(page, size));
        List<UserController.UserResponse> content = result.getContent().stream()
                .map(UserController.UserResponse::from)
                .toList();

        return ApiResponse.ok(new PagedUserResponse(content, result.getTotalPages(), result.getTotalElements()));
    }

    // 대시보드 통계 — OPERATOR, ADMIN
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('PERM_CHALLENGE_CREATE')")
    public ApiResponse<StatsResponse> getStats() {
        long totalUsers = userRepository.count();
        long totalExchanges = rewardExchangeRepository.count();
        return ApiResponse.ok(new StatsResponse(totalUsers, totalExchanges));
    }

    // 계정 정지/해제 — ADMIN only, 데이터 보존 소프트 밴
    @PatchMapping("/users/{id}/ban")
    @PreAuthorize("hasAuthority('PERM_USER_BAN')")
    @Transactional
    @Audited(action = "USER_BAN", targetType = "USER", targetIdSpEL = "#id.toString()")
    public ApiResponse<UserController.UserResponse> banUser(
            @PathVariable Long id,
            @RequestParam boolean ban) {
        User user = userService.findById(id);
        if (ban) user.ban(); else user.unban();
        return ApiResponse.ok(UserController.UserResponse.from(user));
    }

    public record PagedUserResponse(
            List<UserController.UserResponse> content,
            int totalPages,
            long totalElements
    ) {}

    public record StatsResponse(long totalUsers, long totalExchanges) {}
}
