package org.ggne.rc.domain.user.entity;

import lombok.Getter;

import java.util.Set;

/**
 * 사용자 역할(Role)과 그 역할이 보유한 권한(Permission) 집합을 정의한다.
 * JwtAuthenticationFilter에서 이 매핑을 읽어 SecurityContext에 PERM_X를 등록한다.
 */
public enum UserRole {

    // 일반 사용자 — 포인트 적립, 리워드 교환만 가능
    USER(Set.of()),

    // 운영자 — 챌린지/미션/리워드 관리 권한 보유
    OPERATOR(Set.of(
            Permission.PERM_CHALLENGE_CREATE,
            Permission.PERM_MISSION_MANAGE,
            Permission.PERM_REWARD_MANAGE,
            Permission.PERM_USER_READ,
            Permission.PERM_STATS_READ
    )),

    // 관리자 — 전체 권한 보유 (운영자 계정 관리 포함)
    ADMIN(Set.of(
            Permission.PERM_CHALLENGE_CREATE,
            Permission.PERM_CHALLENGE_DELETE,
            Permission.PERM_MISSION_MANAGE,
            Permission.PERM_REWARD_MANAGE,
            Permission.PERM_USER_READ,
            Permission.PERM_USER_BAN,
            Permission.PERM_OPERATOR_MANAGE,
            Permission.PERM_STATS_READ,
            Permission.PERM_AUDIT_READ
    ));

    // Set.of()는 불변 집합 — 런타임에 권한이 추가/삭제될 일이 없음
    @Getter
    private final Set<Permission> permissions;

    UserRole(Set<Permission> permissions) {
        this.permissions = permissions;
    }
}
