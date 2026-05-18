package org.ggne.challenge.domain.mission.repository;

import org.ggne.challenge.domain.mission.entity.MissionLog;
import org.ggne.challenge.domain.mission.entity.MissionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MissionLogRepository extends JpaRepository<MissionLog, Long> {

    List<MissionLog> findByParticipationId(Long participationId);

    // fetch join으로 participation → challenge 한 번에 로드 (N+1 방지)
    @Query("SELECT m FROM MissionLog m JOIN FETCH m.participation p JOIN FETCH p.challenge WHERE m.status = :status ORDER BY m.completedAt DESC")
    List<MissionLog> findByStatusWithDetails(MissionStatus status);
}
