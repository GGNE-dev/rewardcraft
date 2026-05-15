package org.ggne.rc.domain.user.entity;

public enum Permission {

    // 챌린지 도메인
    PERM_CHALLENGE_CREATE,  // 챌린지 생성
    PERM_CHALLENGE_DELETE,  // 챌린지 강제 삭제

    // 미션 도메인
    PERM_MISSION_MANAGE,    // 미션 항목 추가/수정

    // 리워드 도메인
    PERM_REWARD_MANAGE,     // 리워드 등록/재고 수정

    // 사용자 도메인
    PERM_USER_READ,         // 사용자 목록 조회
    PERM_USER_BAN,          // 사용자 계정 정지

    // 운영자 도메인
    PERM_OPERATOR_MANAGE,   // 운영자 계정 생성/삭제

    // 공통
    PERM_STATS_READ,        // 통계 조회
    PERM_AUDIT_READ         // 감사 로그 조회
}
