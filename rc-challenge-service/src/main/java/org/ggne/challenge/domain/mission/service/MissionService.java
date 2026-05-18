package org.ggne.challenge.domain.mission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.mission.entity.MissionLog;
import org.ggne.challenge.domain.mission.entity.OutboxEvent;
import org.ggne.challenge.domain.mission.repository.MissionLogRepository;
import org.ggne.challenge.domain.mission.repository.OutboxEventRepository;
import org.ggne.challenge.domain.participation.entity.Participation;
import org.ggne.challenge.domain.participation.repository.ParticipationRepository;
import org.ggne.challenge.global.exception.BusinessException;
import org.ggne.challenge.global.exception.ErrorCode;
import org.ggne.rc.events.MissionCompletedEvent;
import org.ggne.challenge.global.exception.SystemException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionLogRepository missionLogRepository;
    private final ParticipationRepository participationRepository;
    private final OutboxEventRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topics.mission-completed}")
    private String missionCompletedTopic;


    @Transactional
    public MissionLog complete(Long participationId, long points, String memo) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        participation.addPoints(points);

        MissionLog savedLog = missionLogRepository.save(MissionLog.complete(participation, points, memo));

        MissionCompletedEvent missionCompletedEvent = MissionCompletedEvent.of(
                participation.getUserId(),
                participation.getChallenge().getId(),
                participation.getId(),
                points
        );

        try {
            String payload = objectMapper.writeValueAsString(missionCompletedEvent);
            outboxRepository.save(OutboxEvent.of(missionCompletedTopic, missionCompletedEvent.userId().toString(), payload));
        } catch (JsonProcessingException e) {
            throw new SystemException("이벤트 직렬화 실패", e);
        }

        return savedLog;
    }

    public List<MissionLog> findByParticipationId(Long participationId) {
        return missionLogRepository.findByParticipationId(participationId);
    }
}
