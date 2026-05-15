package org.ggne.challenge.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_event_consumer",
                columnNames = {"event_id", "consumer_group"}
        ),
        indexes = @Index(name = "idx_processed_event_id", columnList = "event_id")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false, length = 100)
    private String eventId;

    @Column(name = "consumer_group", nullable = false, length = 50)
    private String consumerGroup;

    @Column(name = "processed_at", nullable = false)
    private LocalDateTime processedAt;

    public ProcessedEvent(String eventId, String consumerGroup) {
        this.eventId = eventId;
        this.consumerGroup = consumerGroup;
        this.processedAt = LocalDateTime.now();
    }
}
