package org.ggne.rc.domain.participation.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.challenge.entity.Challenge;
import org.ggne.rc.domain.challenge.repository.ChallengeRepository;
import org.ggne.rc.domain.participation.entity.Participation;
import org.ggne.rc.domain.participation.repository.ParticipationRepository;
import org.ggne.rc.domain.user.entity.User;
import org.ggne.rc.domain.user.repository.UserRepository;
import org.ggne.rc.global.exception.ConflictException;
import org.ggne.rc.global.exception.NotFoundException;
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
            throw new ConflictException("이미 참여 중인 챌린지입니다.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("회원"));
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new NotFoundException("챌린지"));

        return participationRepository.save(Participation.join(user, challenge));
    }

    public Participation findById(Long id) {
        return participationRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("참여 정보"));
    }

    public List<Participation> findByUserId(Long userId) {
        return participationRepository.findByUserId(userId);
    }

    // [N+1 데모용 — 비교 후 제거]
    public List<String> getParticipantNicknamesWithNPlusOne(Long challengeId) {
        List<Participation> participations = participationRepository.findByChallengeId(challengeId);
        return participations.stream()
                .map(p -> p.getUser().getNickname())
                .toList();
    }

    // [Fetch Join 데모용 — 비교 후 제거]
    public List<String> getParticipantNicknamesWithFetchJoin(Long challengeId) {
        List<Participation> participations = participationRepository.findByChallengeIdFetchUser(challengeId);
        return participations.stream()
                .map(p -> p.getUser().getNickname())
                .toList();
    }
}
