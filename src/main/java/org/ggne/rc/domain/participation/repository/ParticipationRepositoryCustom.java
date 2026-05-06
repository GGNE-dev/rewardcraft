package org.ggne.rc.domain.participation.repository;

import org.ggne.rc.domain.participation.dto.ParticipantSummaryDto;
import org.ggne.rc.domain.participation.entity.Participation;

import java.util.List;

public interface ParticipationRepositoryCustom {

    List<Participation> findByChallengeWithMissionLogs(Long challengeId);
    List<ParticipantSummaryDto> findParticipantSummary(Long challengeId);
}
