package org.ggne.rc.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.audit.entity.AuditLog;
import org.ggne.rc.domain.audit.repository.AuditLogRepository;
import org.ggne.rc.global.audit.Audited;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

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
}
