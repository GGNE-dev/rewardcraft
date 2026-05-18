package org.ggne.challenge.domain.participation.service;

import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.challenge.entity.Challenge;
import org.ggne.challenge.domain.challenge.repository.ChallengeRepository;
import org.ggne.challenge.domain.participation.dto.ParticipantSummaryDto;
import org.ggne.challenge.domain.participation.entity.Participation;
import org.ggne.challenge.domain.participation.repository.ParticipationRepository;
import org.ggne.challenge.global.exception.BusinessException;
import org.ggne.challenge.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final ChallengeRepository challengeRepository;

    // userId는 JWT 인증이 완료된 값이므로 User 존재 여부 재검증 생략
    // 운영: API Gateway가 JWT 검증 후 X-User-Id 헤더로 전달
    @Transactional
    public Participation join(Long userId, Long challengeId) {
        if (participationRepository.findByUserIdAndChallengeId(userId, challengeId).isPresent()) {
            throw new BusinessException(ErrorCode.ALREADY_PARTICIPATED);
        }
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        return participationRepository.save(Participation.join(userId, challenge));
    }

    public Participation findById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));
    }

    public List<Participation> findByUserId(Long userId) {
        return participationRepository.findByUserId(userId);
    }

    public List<Participation> getParticipantsWithMissions(Long challengeId) {
        return participationRepository.findByChallengeWithMissionLogs(challengeId);
    }

    public List<ParticipantSummaryDto> getParticipantSummary(Long challengeId) {
        return participationRepository.findParticipantSummary(challengeId);
    }

    // rewardcraft-api의 RewardExchangeService가 Feign으로 호출하는 내부 메서드
    public Long getUserTotalPoints(Long userId) {
        return participationRepository.sumTotalPointsByUserId(userId);
    }
}
