package org.ggne.notification.consumer;

import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.events.MissionCompletedEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MissionCompletedConsumer {

    @KafkaListener(
            topics = "${kafka.topics.mission-completed}",
            groupId = "notification-service-group"
    )
    public void handle(MissionCompletedEvent event, Acknowledgment ack) {
        try {
            // TODO: 카카오 알림톡, 이메일 등 실제 발송으로 교체
            log.info("[알림] 미션 완료 알림 발송: userId={}, points={}", event.userId(), event.pointsEarned());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("알림 처리 실패: eventId={}", event.eventId(), e);
        }
    }
}
