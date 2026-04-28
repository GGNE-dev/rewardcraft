package org.ggne.rc.domain.user.entity;

public enum UserRole {
    USER,       // 일반 사용자
    OPERATOR,   // 챌린지 개설/관리 권한을 가진 운영자
    ADMIN       // 전체 시스템 관리 권한 (운영자 계정 관리 포함)
}
