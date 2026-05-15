package org.ggne.rc.domain.mission.event;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.domain.notification.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private static final String GROUP_ID = "notification-group";

    // PointsConsumer와 groupId가 다르므로 같은 이벤트를 독립적으로 수신
    // notification-group이 느려지거나 장애가 나도 points-group에 영향 없음
    private final NotificationService notificationService;

    @KafkaListener(
            topics = "${kafka.topics.mission-completed}",
            groupId = GROUP_ID,
            concurrency = "3"
    )
    public void handle(MissionCompletedEvent event, Acknowledgment ack) {
        try {
            // TODO: MSA 분리 시 별도 알림 서비스로 교체 예정
            notificationService.sendMissionCompleteNotification(event.userId(), event.pointsEarned());

            ack.acknowledge();

        } catch (Exception e) {
            log.error("알림 처리 실패: eventId={}", event.eventId(), e);
        }
    }
}
