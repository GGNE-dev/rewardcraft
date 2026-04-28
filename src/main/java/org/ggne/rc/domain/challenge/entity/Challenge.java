package org.ggne.rc.domain.challenge.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "challenges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Challenge(String title, String description,
                     LocalDateTime startAt, LocalDateTime endAt) {
        if (endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("종료일이 시작일보다 앞일 수 없습니다.");
        }
        this.title = title;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.status = ChallengeStatus.UPCOMING;
        this.createdAt = LocalDateTime.now();
    }

    public void activate() {
        this.status = ChallengeStatus.ACTIVE;
    }

    public void end() {
        this.status = ChallengeStatus.ENDED;
    }
}
