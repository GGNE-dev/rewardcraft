package org.ggne.rc.domain.user.controller;

import org.ggne.rc.global.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    // OPERATOR, ADMIN 모두 접근 가능
    @GetMapping("/challenges")
    @PreAuthorize("hasAuthority('PERM_CHALLENGE_CREATE')")
    public ApiResponse<String> challengeManage() {
        return ApiResponse.ok("챌린지 관리 접근 성공");
    }

    // ADMIN만 접근 가능
    @GetMapping("/users/ban")
    @PreAuthorize("hasAuthority('PERM_USER_BAN')")
    public ApiResponse<String> userBan() {
        return ApiResponse.ok("사용자 정지 접근 성공");
    }

    // ADMIN만 접근 가능
    @GetMapping("/operators")
    @PreAuthorize("hasAuthority('PERM_OPERATOR_MANAGE')")
    public ApiResponse<String> operatorManage() {
        return ApiResponse.ok("운영자 관리 접근 성공");
    }
}
