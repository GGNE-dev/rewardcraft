package org.ggne.rc.domain.participation.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.challenge.entity.Challenge;
import org.ggne.rc.domain.challenge.repository.ChallengeRepository;
import org.ggne.rc.domain.participation.dto.ParticipantSummaryDto;
import org.ggne.rc.domain.participation.entity.Participation;
import org.ggne.rc.domain.participation.repository.ParticipationRepository;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.repository.UserRepository;
import org.ggne.rc.global.exception.RCBusinessException;
import org.ggne.rc.global.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParticipationService {

    private final ParticipationRepository participationRepository;
    private final UserRepository userRepository;
    private final ChallengeRepository challengeRepository;

    @Transactional
    public Participation join(Long userId, Long challengeId) {
        // DB 레벨 unique 제약이 있지만, 명시적 중복 검사로 의미 있는 에러 메시지 제공
        if (participationRepository.findByUserIdAndChallengeId(userId, challengeId).isPresent()) {
            throw new RCBusinessException(ErrorCode.ALREADY_PARTICIPATED);
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.USER_NOT_FOUND));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.CHALLENGE_NOT_FOUND));

        return participationRepository.save(Participation.join(user, challenge));
    }

    public Participation findById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.PARTICIPATION_NOT_FOUND));
    }

    public List<Participation> findByUserId(Long userId) {
        return participationRepository.findByUserId(userId);
    }

    // Fetch Join 활용 — 엔티티 객체 그래프가 필요한 내부 로직에서 사용
    public List<Participation> getParticipantsWithMissions(Long challengeId) {
        return participationRepository.findByChallengeWithMissionLogs(challengeId);
    }

    // DTO Projection 활용 — 화면 출력용 요약 데이터 반환
    public List<ParticipantSummaryDto> getParticipantSummary(Long challengeId) {
        return participationRepository.findParticipantSummary(challengeId);
    }

    // [N+1 데모용 — 비교 후 제거]
    @Deprecated
    public List<String> getParticipantNicknamesWithNPlusOne(Long challengeId) {
        List<Participation> participations = participationRepository.findByChallengeId(challengeId);
        return participations.stream()
                .map(p -> p.getUser().getNickname())
                .toList();
    }

    // [Fetch Join 데모용 — 비교 후 제거]
    @Deprecated
    public List<String> getParticipantNicknamesWithFetchJoin(Long challengeId) {
        List<Participation> participations = participationRepository.findByChallengeIdFetchUser(challengeId);
        return participations.stream()
                .map(p -> p.getUser().getNickname())
                .toList();
    }
}
