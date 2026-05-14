package org.ggne.rc.domain.challenge.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.challenge.entity.Challenge;
import org.ggne.rc.domain.challenge.entity.ChallengeStatus;
import org.ggne.rc.domain.challenge.repository.ChallengeRepository;
import org.ggne.rc.global.exception.RCBusinessException;
import org.ggne.rc.global.exception.ErrorCode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    // 활성 챌린지 목록은 ID가 없으니 키를 고정값으로
    @Cacheable(value = "challenge", key = "'active'")
    public List<Challenge> findActive() {
        return challengeRepository.findByStatus(ChallengeStatus.ACTIVE);
    }

    // "challenge" 캐시에 저장. 키 = challengeId 값
    @Cacheable(value = "challenge", key = "#challengeId")
    public Challenge findById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
    }

    @Transactional
    public Challenge create(String title, String description,
                            LocalDateTime startAt, LocalDateTime endAt) {
        return challengeRepository.save(
                Challenge.builder()
                        .title(title)
                        .description(description)
                        .startAt(startAt)
                        .endAt(endAt)
                        .build()
        );
    }
}
