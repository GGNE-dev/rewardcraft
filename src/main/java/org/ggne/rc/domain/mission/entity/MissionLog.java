package org.ggne.rc.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ggne.rc.domain.participation.entity.Participation;

import java.time.LocalDateTime;

@Entity
@Table(name = "mission_logs")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MissionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "participation_id", nullable = false)
    private Participation participation;

    @Column(name = "completed_at", nullable = false)
    private LocalDateTime completedAt;

    @Column(name = "points_earned", nullable = false)
    private Long pointsEarned;

    @Column(length = 500)
    private String memo;

    public static MissionLog complete(Participation participation, long points, String memo) {
        MissionLog log = new MissionLog();
        log.participation = participation;
        log.completedAt = LocalDateTime.now();
        log.pointsEarned = points;
        log.memo = memo;
        return log;
    }
}
