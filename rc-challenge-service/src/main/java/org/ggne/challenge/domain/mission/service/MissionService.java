package org.ggne.challenge.domain.mission.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.mission.entity.MissionLog;
import org.ggne.challenge.domain.mission.entity.MissionStatus;
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

    // 미션 완료 신청 — PENDING 상태로 저장, 포인트 지급 없음
    @Transactional
    public MissionLog complete(Long participationId, long points, String memo) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));

        return missionLogRepository.save(MissionLog.complete(participation, points, memo));
    }

    // 운영자/관리자 승인 — APPROVED로 변경, 포인트 지급, Kafka 이벤트 발행
    @Transactional
    public MissionLog approve(Long missionLogId) {
        MissionLog log = missionLogRepository.findById(missionLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));
        if (log.getStatus() != MissionStatus.PENDING) {
            throw new BusinessException(ErrorCode.MISSION_ALREADY_PROCESSED);
        }

        log.approve();
        Participation participation = log.getParticipation();
        participation.addPoints(log.getPointsEarned());

        MissionCompletedEvent event = MissionCompletedEvent.of(
                participation.getUserId(),
                participation.getChallenge().getId(),
                participation.getId(),
                log.getPointsEarned()
        );
        try {
            String payload = objectMapper.writeValueAsString(event);
            outboxRepository.save(OutboxEvent.of(missionCompletedTopic, event.userId().toString(), payload));
        } catch (JsonProcessingException e) {
            throw new SystemException("이벤트 직렬화 실패", e);
        }

        return log;
    }

    // 운영자/관리자 거절 — REJECTED로 변경, 포인트 지급 없음
    @Transactional
    public MissionLog reject(Long missionLogId) {
        MissionLog log = missionLogRepository.findById(missionLogId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MISSION_NOT_FOUND));
        if (log.getStatus() != MissionStatus.PENDING) {
            throw new BusinessException(ErrorCode.MISSION_ALREADY_PROCESSED);
        }
        log.reject();
        return log;
    }

    public List<MissionLog> findByParticipationId(Long participationId) {
        return missionLogRepository.findByParticipationId(participationId);
    }

    public List<MissionLog> findAllPending() {
        return missionLogRepository.findByStatusWithDetails(MissionStatus.PENDING);
    }
}
