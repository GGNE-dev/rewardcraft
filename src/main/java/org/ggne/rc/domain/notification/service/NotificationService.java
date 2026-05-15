package org.ggne.rc.domain.notification.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class NotificationService {

    // TODO: 추후 MSA 분리 시 카카오 알림톡, 이메일 등 실제 발송으로 교체 예정
    public void sendMissionCompleteNotification(Long userId, long points) {
        log.info("[알림 stub] 미션 완료 알림 발송: userId={}, points={}", userId, points);
    }
}
