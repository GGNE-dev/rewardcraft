package org.ggne.rc.domain.challenge.service;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.challenge.entity.Challenge;
import org.ggne.rc.domain.challenge.entity.ChallengeStatus;
import org.ggne.rc.domain.challenge.repository.ChallengeRepository;
import org.ggne.rc.global.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeService {

    private final ChallengeRepository challengeRepository;

    public List<Challenge> findActive() {
        return challengeRepository.findByStatus(ChallengeStatus.ACTIVE);
    }

    public Challenge findById(Long id) {
        return challengeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("챌린지"));
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
