package org.ggne.rc.domain.participation.repository;

import org.ggne.rc.domain.participation.entity.Participation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ParticipationRepository extends JpaRepository<Participation, Long>, ParticipationRepositoryCustom {

    Optional<Participation> findByUserIdAndChallengeId(Long userId, Long challengeId);

    List<Participation> findByUserId(Long userId);

    List<Participation> findByChallengeId(Long challengeId);

    @Query("SELECT p FROM Participation p JOIN FETCH p.user WHERE p.challenge.id = :challengeId")
    List<Participation> findByChallengeIdFetchUser(@Param("challengeId") Long challengeId);

    // 분산 락: 리워드 교환 가능 여부 판단을 위해 사용자의 전체 보유 포인트 합산
    @Query("SELECT COALESCE(SUM(p.totalPoints), 0) FROM Participation p WHERE p.user.id = :userId")
    Long sumTotalPointsByUserId(@Param("userId") Long userId);
}
