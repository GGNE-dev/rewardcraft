package org.ggne.rc.domain.participation.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.mission.entity.QMissionLog;
import org.ggne.rc.domain.participation.dto.ParticipantSummaryDto;
import org.ggne.rc.domain.participation.entity.Participation;
import org.ggne.rc.domain.participation.entity.QParticipation;
import org.ggne.rc.domain.user.entity.QUser;

import java.util.List;

@RequiredArgsConstructor
public class ParticipationRepositoryImpl implements ParticipationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    /**
     * Fetch Join을 통한 참여자 + 미션 로그 조회 (엔티티 객체 그래프 필요 시)
     */
    @Override
    public List<Participation> findByChallengeWithMissionLogs(Long challengeId) {
        QParticipation participation = QParticipation.participation;
        QMissionLog missionLog = QMissionLog.missionLog;

        return queryFactory.selectFrom(participation)
                // fetchJoin(): missionLogs 컬렉션을 영속성 컨텍스트에 미리 채움
                .leftJoin(participation.missionLogs, missionLog).fetchJoin()
                .where(participation.challenge.id.eq(challengeId))
                .distinct()
                .fetch();
    }

    /**
     *  DTO Projection을 통한 화면에 필요한 컬럼만 직접 조회 (읽기 전용)
     */
    @Override
    public List<ParticipantSummaryDto> findParticipantSummary(Long challengeId) {
        QParticipation participation = QParticipation.participation;
        QUser user = QUser.user;
        QMissionLog missionLog = QMissionLog.missionLog;

        return queryFactory
                // Projections.constructor: DTO 생성자를 직접 호출
                // 인자 순서가 ParticipantSummaryDto record 선언 순서와 일치해야 함
                .select(Projections.constructor(
                        ParticipantSummaryDto.class,
                        user.id,
                        user.nickname,
                        participation.totalPoints,
                        missionLog.count()          // COUNT(mission_log.id) 집계
                ))
                .from(participation)
                .join(participation.user, user)                     // user는 반드시 존재 → INNER JOIN
                .leftJoin(participation.missionLogs, missionLog)    // 미션 없는 참여자도 포함
                .where(participation.challenge.id.eq(challengeId))
                // PostgreSQL은 SELECT의 비집계 컬럼을 모두 GROUP BY에 명시 필수
                .groupBy(user.id, user.nickname, participation.totalPoints)
                .fetch();
    }
}
