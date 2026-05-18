package org.ggne.challenge.domain.mission.repository;

import org.ggne.challenge.domain.mission.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {

    // published=false인 것만 생성 순서대로 = 순서 보장이 중요
    List<OutboxEvent> findByPublishedFalseOrderByCreatedAtAsc();
}
