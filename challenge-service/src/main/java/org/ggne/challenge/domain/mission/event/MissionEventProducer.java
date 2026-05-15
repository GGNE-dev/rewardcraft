package org.ggne.challenge.domain.mission.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.events.MissionCompletedEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class MissionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topics.mission-completed}")
    private String missionCompletedTopic;

    public void publishMissionCompleted(MissionCompletedEvent event) {
        kafkaTemplate.send(missionCompletedTopic, event.userId().toString(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Kafka 이벤트 발행 실패: eventId={}, error={}", event.eventId(), ex.getMessage());
                    } else {
                        log.debug("Kafka 발행 성공: eventId={}, partition={}, offset={}",
                                event.eventId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }
}
