package org.ggne.challenge.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.mission.entity.MissionLog;
import org.ggne.challenge.domain.mission.event.MissionEventProducer;
import org.ggne.challenge.domain.mission.repository.MissionLogRepository;
import org.ggne.challenge.domain.participation.entity.Participation;
import org.ggne.challenge.domain.participation.repository.ParticipationRepository;
import org.ggne.challenge.global.exception.BusinessException;
import org.ggne.challenge.global.exception.ErrorCode;
import org.ggne.rc.events.MissionCompletedEvent;
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

    @Transactional
    public MissionLog complete(Long participationId, long points, String memo) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        participation.addPoints(points);

        MissionLog savedLog = missionLogRepository.save(MissionLog.complete(participation, points, memo));

        eventProducer.publishMissionCompleted(
                MissionCompletedEvent.of(
                        participation.getUserId(),
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
