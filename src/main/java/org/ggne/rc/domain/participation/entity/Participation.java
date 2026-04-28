package org.ggne.rc.domain.participation.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ggne.rc.domain.challenge.entity.Challenge;
import org.ggne.rc.domain.user.entity.User;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "participations",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "challenge_id"})  // 같은 챌린지 중복 참여 방지
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Participation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)  // LAZY: 실제 접근 시점에만 쿼리 (N+1 문제는 Ch 01에서 해결)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;

    @Column(name = "joined_at", nullable = false, updatable = false)
    private LocalDateTime joinedAt;

    @Column(name = "total_points", nullable = false)
    private Long totalPoints;

    public static Participation join(User user, Challenge challenge) {
        Participation p = new Participation();
        p.user = user;
        p.challenge = challenge;
        p.joinedAt = LocalDateTime.now();
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
