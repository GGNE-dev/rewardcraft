package org.ggne.rc.domain.mission.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.domain.challenge.service.RankingService;
import org.ggne.rc.events.MissionCompletedEvent;
import org.ggne.rc.domain.mission.entity.ProcessedEvent;
import org.ggne.rc.domain.mission.repository.ProcessedEventRepository;
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
            // 이미 처리한 이벤트이면 스킵
            if (processedEventRepository.existsByEventIdAndConsumerGroup(event.eventId(), GROUP_ID)) {
                log.warn("중복 이벤트 스킵 처리 : eventId={}", event.eventId());
                ack.acknowledge();

                return;
            }

            rankingService.addPoints(event.challengeId(), event.userId(), event.pointsEarned());

            // 처리 완료 기록 (이후 중복 수신 시 스킵)
            processedEventRepository.save(new ProcessedEvent(event.eventId(), GROUP_ID));

            // 처리 완료 후, 오프셋 커밋
            ack.acknowledge();

        } catch (Exception e) {
            log.error("이벤트 처리 실패 : eventId={}", event.eventId(), e);
        }
    }
}
