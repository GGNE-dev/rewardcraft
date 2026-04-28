package org.ggne.rc.domain.mission.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.mission.entity.MissionLog;
import org.ggne.rc.domain.mission.repository.MissionLogRepository;
import org.ggne.rc.domain.participation.entity.Participation;
import org.ggne.rc.domain.participation.repository.ParticipationRepository;
import org.ggne.rc.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MissionService {

    private final MissionLogRepository missionLogRepository;
    private final ParticipationRepository participationRepository;

    @Transactional
    public MissionLog complete(Long participationId, long points, String memo) {
        Participation participation = participationRepository.findById(participationId)
                .orElseThrow(() -> new NotFoundException("참여 정보"));

        participation.addPoints(points);   // 더티 체킹으로 totalPoints 자동 UPDATE
        return missionLogRepository.save(MissionLog.complete(participation, points, memo));
    }

    public List<MissionLog> findByParticipationId(Long participationId) {
        return missionLogRepository.findByParticipationId(participationId);
    }
}
