package org.ggne.challenge.domain.challenge.repository;

import org.ggne.challenge.domain.challenge.entity.Challenge;
import org.ggne.challenge.domain.challenge.entity.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByStatus(ChallengeStatus status);
}
