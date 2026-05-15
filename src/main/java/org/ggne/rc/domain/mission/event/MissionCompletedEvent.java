package org.ggne.rc.domain.mission.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record MissionCompletedEvent(
        String eventId,
        Long userId,
        Long challengeId,
        Long participationId,
        long pointsEarned,
        LocalDateTime completedAt
) {
    
    // 정적 팩토리 메소드 생성
    public static MissionCompletedEvent of(Long userId, Long challengeId, Long participationId, long points) {
        return new MissionCompletedEvent(
                UUID.randomUUID().toString(),
                userId,
                challengeId,
                participationId,
                points,
                LocalDateTime.now()
        );
    }
}
