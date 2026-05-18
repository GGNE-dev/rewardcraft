package org.ggne.challenge.domain.participation.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.mission.entity.QMissionLog;
import org.ggne.challenge.domain.participation.dto.ParticipantSummaryDto;
import org.ggne.challenge.domain.participation.entity.Participation;
import org.ggne.challenge.domain.participation.entity.QParticipation;

import java.util.List;

@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Participation> findByChallengeWithMissionLogs(Long challengeId) {
        QParticipation participation = QParticipation.participation;
        QMissionLog missionLog = QMissionLog.missionLog;

        return queryFactory.selectFrom(participation)
                .leftJoin(participation.missionLogs, missionLog).fetchJoin()
                .where(participation.challenge.id.eq(challengeId))
                .distinct()
                .fetch();
    }

    // User BC 분리로 nickname 조회 불가 → userId + 포인트 + 미션 수만 반환
    @Override
    public List<ParticipantSummaryDto> findParticipantSummary(Long challengeId) {
        QParticipation participation = QParticipation.participation;
        QMissionLog missionLog = QMissionLog.missionLog;

        return queryFactory
                .select(Projections.constructor(
                        ParticipantSummaryDto.class,
                        participation.userId,
                        participation.totalPoints,
                        missionLog.count()
                ))
                .from(participation)
                .leftJoin(participation.missionLogs, missionLog)
                .where(participation.challenge.id.eq(challengeId))
                .groupBy(participation.userId, participation.totalPoints)
                .fetch();
    }
}
