package org.ggne.challenge.domain.mission.relay;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.challenge.domain.mission.entity.OutboxEvent;
import org.ggne.challenge.domain.mission.repository.OutboxEventRepository;
import org.ggne.rc.events.MissionCompletedEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;


@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxRelay {

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final PlatformTransactionManager transactionManager;

    @Scheduled(fixedDelay = 1000)
    public void relay() {
        outboxEventRepository.findByPublishedFalseOrderByCreatedAtAsc().forEach(this::publishAndMark);
    }

    private void publishAndMark(OutboxEvent outbox) {
        try {
            MissionCompletedEvent event = objectMapper.readValue(outbox.getPayload(), MissionCompletedEvent.class);

            // 블로킹 발행 — 브로커 확인 후에 published 마킹
            kafkaTemplate.send(outbox.getTopic(), outbox.getPartitionKey(), event).get();
            log.info("Outbox 발행 완료: outboxId={}, topic={}, partitionKey={}",
                    outbox.getId(), outbox.getTopic(), outbox.getPartitionKey());

            // 발행 성공
            new TransactionTemplate(transactionManager).execute(status -> {
               outboxEventRepository.findById(outbox.getId()).ifPresent(OutboxEvent::markPublished);
               return null;
            });

        } catch (Exception e) {
            log.error("Outbox 발행 실패 (다음 폴링 때 재시도): outboxId={}, error={}", outbox.getId(), e.getMessage());
        }
    }
}
