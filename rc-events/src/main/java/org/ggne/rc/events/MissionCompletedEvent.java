package org.ggne.rc.events;

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
