package org.ggne.challenge.domain.mission.entity;

public enum MissionStatus {
    PENDING,   // 승인 대기
    APPROVED,  // 승인됨 (포인트 지급 완료)
    REJECTED   // 거절됨
}
