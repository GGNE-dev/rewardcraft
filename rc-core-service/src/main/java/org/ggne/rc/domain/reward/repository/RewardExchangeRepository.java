package org.ggne.rc.domain.reward.repository;

import org.ggne.rc.domain.reward.entity.RewardExchange;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RewardExchangeRepository extends JpaRepository<RewardExchange, Long> {

    List<RewardExchange> findByUserIdOrderByCreatedAtDesc(Long userId);
}
