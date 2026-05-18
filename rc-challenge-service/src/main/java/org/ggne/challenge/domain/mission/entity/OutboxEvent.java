package org.ggne.challenge.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ggne.challenge.global.entity.BaseEntity;


@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String topic;

    @Column(nullable = false)
    private String partitionKey;   // userId — 같은 유저 이벤트의 파티션 순서 보장

    @Column(columnDefinition = "TEXT", nullable = false)
    private String payload;        // MissionCompletedEvent JSON 직렬화 값

    @Column(nullable = false)
    private boolean published = false;

    public static OutboxEvent of(String topic, String partitionKey, String payload) {
        OutboxEvent e = new OutboxEvent();
        e.topic = topic;
        e.partitionKey = partitionKey;
        e.payload = payload;

        return e;
    }

    public void markPublished() {
        this.published = true;
    }
}
