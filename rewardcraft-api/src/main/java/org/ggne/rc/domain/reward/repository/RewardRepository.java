package org.ggne.rc.domain.reward.repository;

import jakarta.persistence.LockModeType;
import org.ggne.rc.domain.reward.entity.Reward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RewardRepository extends JpaRepository<Reward, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reward r WHERE r.id = :id")
    Optional<Reward> findByIdForUpdate(@Param("id") Long id);
}
