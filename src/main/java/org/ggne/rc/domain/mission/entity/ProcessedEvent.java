package org.ggne.rc.domain.mission.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "processed_events",
        uniqueConstraints = @UniqueConstraint(    // event_id 단독 unique가 아닌 (event_id, consumer_group) 복합 unique.
                name = "uq_event_consumer",
                columnNames = {"event_id", "consumer_group"}
        ),
        // 단일 컬럼 인덱스로 복합 unique 인덱스보다 조회 효율 높이고자 함.
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

    // 어떤 컨슈머 그룹이 처리했는지 기록. (eventId, consumerGroup) 조합으로 중복 처리를 그룹별로 독립 방지.
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
