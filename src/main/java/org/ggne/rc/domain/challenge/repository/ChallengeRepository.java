package org.ggne.rc.domain.challenge.repository;

import org.ggne.rc.domain.challenge.entity.Challenge;
import org.ggne.rc.domain.challenge.entity.ChallengeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChallengeRepository extends JpaRepository<Challenge, Long> {

    List<Challenge> findByStatus(ChallengeStatus status);
}
