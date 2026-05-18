package org.ggne.challenge.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.challenge.domain.challenge.service.RankingService;
import org.ggne.challenge.domain.participation.entity.Participation;
import org.ggne.challenge.domain.participation.repository.ParticipationRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 앱 시작 시 PostgreSQL의 participation.total_points를 Redis Sorted Set에 동기화.
 * data.sql로 시딩한 데이터가 Kafka 없이도 랭킹 조회에 즉시 반영되도록 함.
 * 운영 환경에서는 배치 잡 또는 별도 동기화 파이프라인으로 대체 필요.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RankingInitializer implements ApplicationRunner {

    private final ParticipationRepository participationRepository;
    private final RankingService rankingService;

    @Override
    public void run(ApplicationArguments args) {
        List<Participation> all = participationRepository.findAll();

        // 기존 Redis 키를 지우고 DB 기준으로 재구성
        // (재시작 시 중복 누적 방지)
        all.stream()
                .map(p -> p.getChallenge().getId())
                .distinct()
                .forEach(challengeId -> rankingService.clearRanking(challengeId));

        for (Participation p : all) {
            if (p.getTotalPoints() > 0) {
                rankingService.addPoints(p.getChallenge().getId(), p.getUserId(), p.getTotalPoints());
            }
        }

        log.info("랭킹 Redis 초기화 완료: {}개 participation 동기화", all.size());
    }
}
