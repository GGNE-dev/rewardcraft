package org.ggne.challenge.domain.participation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ggne.challenge.domain.challenge.entity.Challenge;
import org.ggne.challenge.domain.mission.entity.MissionLog;
import org.ggne.challenge.global.entity.BaseEntity;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "participations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id"})
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MSA 경계: User는 rewardcraft-api 소속. 서비스 간 JPA Join 금지.
    // userId만 저장하고, 필요 시 rewardcraft-api Internal API로 조회.
    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "total_points", nullable = false)
    private Long totalPoints;

    @OneToMany(mappedBy = "participation", fetch = FetchType.LAZY)
    private List<MissionLog> missionLogs = new ArrayList<>();

    public static Participation join(Long userId, Challenge challenge) {
        Participation p = new Participation();
        p.userId = userId;
        p.challenge = challenge;
        p.totalPoints = 0L;
        return p;
    }

    public void addPoints(long points) {
        if (points <= 0) throw new IllegalArgumentException("포인트는 양수여야 합니다.");
        this.totalPoints += points;
    }

    public void subtractPoints(long points) {
        if (points <= 0) throw new IllegalArgumentException("차감 포인트는 양수여야 합니다.");
        if (this.totalPoints < points) throw new IllegalArgumentException("포인트가 부족합니다.");
        this.totalPoints -= points;
    }
}
