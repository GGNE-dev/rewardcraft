package org.ggne.challenge.domain.mission.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.challenge.domain.challenge.service.RankingService;
import org.ggne.challenge.domain.mission.entity.ProcessedEvent;
import org.ggne.challenge.domain.mission.repository.ProcessedEventRepository;
import org.ggne.rc.events.MissionCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PointsConsumer {

    private static final String GROUP_ID = "points-group";

    private final RankingService rankingService;
    private final ProcessedEventRepository processedEventRepository;

    @KafkaListener(
            topics = "${kafka.topics.mission-completed}",
            groupId = GROUP_ID,
            concurrency = "3"
    )
    public void handle(MissionCompletedEvent event,
                       @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                       @Header(KafkaHeaders.OFFSET) Long offset,
                       Acknowledgment ack) {

        log.info("이벤트 수신: eventId={}, partition={}, offset={}", event.eventId(), partition, offset);

        try {
            if (processedEventRepository.existsByEventIdAndConsumerGroup(event.eventId(), GROUP_ID)) {
                log.warn("중복 이벤트 스킵: eventId={}", event.eventId());
                ack.acknowledge();
                return;
            }

            rankingService.addPoints(event.challengeId(), event.userId(), event.pointsEarned());
            processedEventRepository.save(new ProcessedEvent(event.eventId(), GROUP_ID));
            ack.acknowledge();

        } catch (Exception e) {
            log.error("이벤트 처리 실패: eventId={}", event.eventId(), e);
        }
    }
}
