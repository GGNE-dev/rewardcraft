package org.ggne.challenge.domain.mission.repository;

import org.ggne.challenge.domain.mission.entity.MissionLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MissionLogRepository extends JpaRepository<MissionLog, Long> {

    List<MissionLog> findByParticipationId(Long participationId);
}
