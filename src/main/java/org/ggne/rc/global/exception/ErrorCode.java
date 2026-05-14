package org.ggne.rc.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // ── 인증/권한 ──────────────────────────────────────────────
    ACCESS_DENIED("AUTH_001", "접근 권한이 없습니다.", 403),
    INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰입니다.", 401),
    TOKEN_REUSE_DETECTED("AUTH_003", "토큰 재사용이 감지됐습니다. 다시 로그인해주세요.", 401),

    // ── 사용자 ────────────────────────────────────────────────
    USER_NOT_FOUND("USR_001", "사용자를 찾을 수 없습니다.", 404),

    // ── 챌린지 ────────────────────────────────────────────────
    CHALLENGE_NOT_FOUND("CHL_001", "챌린지를 찾을 수 없습니다.", 404),

    // ── 참여 ──────────────────────────────────────────────────
    PARTICIPATION_NOT_FOUND("PTC_001", "참여 정보를 찾을 수 없습니다.", 404),
    ALREADY_PARTICIPATED("PTC_002", "이미 참여 중인 챌린지입니다.", 409),

    // ── 리워드 ────────────────────────────────────────────────
    REWARD_NOT_FOUND("RWD_001", "리워드를 찾을 수 없습니다.", 404),
    OUT_OF_STOCK("RWD_002", "해당 리워드가 품절됐습니다.", 409),
    INSUFFICIENT_POINTS("RWD_003", "포인트가 부족합니다.", 400),
    LOCK_ACQUISITION_FAILED("RWD_004", "잠시 후 다시 시도해주세요.", 429);

    private final String code;      // 클라이언트가 에러 종류를 구분하는 코드
    private final String message;   // 사용자에게 보여줄 메시지
    private final int status;       // HTTP 상태 코드
}
