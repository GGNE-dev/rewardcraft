package org.ggne.challenge.domain.participation.dto;

// nickname 제거: User BC(rewardcraft-api)가 소유하는 데이터.
// 필요 시 클라이언트가 rewardcraft-api의 User API를 별도 호출.
public record ParticipantSummaryDto(
        Long userId,
        Long totalPoints,
        Long missionCount
) {}
