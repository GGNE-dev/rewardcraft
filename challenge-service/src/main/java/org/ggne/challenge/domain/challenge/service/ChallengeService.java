package org.ggne.challenge.domain.challenge.service;

import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.challenge.entity.Challenge;
import org.ggne.challenge.domain.challenge.entity.ChallengeStatus;
import org.ggne.challenge.domain.challenge.repository.ChallengeRepository;
import org.ggne.challenge.global.exception.BusinessException;
import org.ggne.challenge.global.exception.ErrorCode;
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

    @Cacheable(value = "challenge", key = "'active'")
    public List<Challenge> findActive() {
        return challengeRepository.findByStatus(ChallengeStatus.ACTIVE);
    }

    @Cacheable(value = "challenge", key = "#challengeId")
    public Challenge findById(Long challengeId) {
        return challengeRepository.findById(challengeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHALLENGE_NOT_FOUND));
    }

    @Transactional
    public Challenge create(String title, String description,
                            LocalDateTime startAt, LocalDateTime endAt) {
        return challengeRepository.save(
                Challenge.builder()
                        .title(title).description(description)
                        .startAt(startAt).endAt(endAt)
                        .build()
        );
    }
}
