package org.ggne.challenge.domain.participation.repository;

import org.ggne.challenge.domain.participation.dto.ParticipantSummaryDto;
import org.ggne.challenge.domain.participation.entity.Participation;

import java.util.List;

public interface ParticipationRepositoryCustom {

    List<Participation> findByChallengeWithMissionLogs(Long challengeId);
    List<ParticipantSummaryDto> findParticipantSummary(Long challengeId);
}
