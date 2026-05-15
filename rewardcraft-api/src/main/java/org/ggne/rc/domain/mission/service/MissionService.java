package org.ggne.rc.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.mission.entity.MissionLog;
import org.ggne.rc.events.MissionCompletedEvent;
import org.ggne.rc.domain.mission.event.MissionEventProducer;
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
    private final MissionEventProducer eventProducer;


    // TODO : 추후 Outbox 패턴으로 개선 필요. (트랜잭션과 이벤트의 불일치 현상 이슈)
    @Transactional
    public MissionLog complete(Long participationId, long points, String memo) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        participation.addPoints(points);

        MissionLog savedLog = missionLogRepository.save(MissionLog.complete(participation, points, memo));

        // Redis 랭킹 갱신 (Redis(sync) -> Kafka(async)로 변경)
        eventProducer.publishMissionCompleted(
                MissionCompletedEvent.of(
                        participation.getUser().getId(),
                        participation.getChallenge().getId(),
                        participation.getId(),
                        points
                )
        );

        return savedLog;
    }

    public List<MissionLog> findByParticipationId(Long participationId) {
        return missionLogRepository.findByParticipationId(participationId);
    }
}
