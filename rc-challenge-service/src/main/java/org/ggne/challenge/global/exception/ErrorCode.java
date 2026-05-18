package org.ggne.challenge.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    CHALLENGE_NOT_FOUND("CHL_001", "챌린지를 찾을 수 없습니다.", 404),
    PARTICIPATION_NOT_FOUND("PTC_001", "참여 정보를 찾을 수 없습니다.", 404),
    ALREADY_PARTICIPATED("PTC_002", "이미 참여 중인 챌린지입니다.", 409);

    private final String code;
    private final String message;
    private final int status;
}
