package org.ggne.rc.domain.participation.dto;

public record ParticipantSummaryDto(
        Long userId,
        String nickname,
        Long totalPoints,
        Long missionCount
) {
}
