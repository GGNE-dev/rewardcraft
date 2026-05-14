package org.ggne.rc.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.challenge.service.RankingService;
import org.ggne.rc.domain.mission.entity.MissionLog;
import org.ggne.rc.domain.mission.repository.MissionLogRepository;
import org.ggne.rc.domain.participation.entity.Participation;
import org.ggne.rc.domain.participation.repository.ParticipationRepository;
import org.ggne.rc.global.exception.RCBusinessException;
import org.ggne.rc.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionLogRepository missionLogRepository;
    private final ParticipationRepository participationRepository;
    private final RankingService rankingService;
    // ⚠️ Ch 05에서 rankingService → MissionEventProducer(Kafka)로 교체됨

    @Transactional
    public MissionLog complete(Long participationId, long points, String memo) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        participation.addPoints(points);

        // Redis 랭킹 갱신 (Ch 05에서 Kafka 비동기 처리로 교체)
        rankingService.addPoints(
                participation.getChallenge().getId(),
                participation.getUser().getId(),
                points
        );

        return missionLogRepository.save(MissionLog.complete(participation, points, memo));
    }

    public List<MissionLog> findByParticipationId(Long participationId) {
        return missionLogRepository.findByParticipationId(participationId);
    }
}
