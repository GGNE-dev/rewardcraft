package org.ggne.challenge.domain.participation.repository;

import org.ggne.challenge.domain.participation.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long>, ParticipationRepositoryCustom {

    Optional<Participation> findByUserIdAndChallengeId(Long userId, Long challengeId);

    List<Participation> findByUserId(Long userId);

    List<Participation> findByChallengeId(Long challengeId);

    // userId가 이제 @Column Long으로 바뀌었으므로 p.userId로 조회
    @Query("SELECT COALESCE(SUM(p.totalPoints), 0) FROM Participation p WHERE p.userId = :userId")
    Long sumTotalPointsByUserId(@Param("userId") Long userId);
}
