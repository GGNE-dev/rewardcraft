package org.ggne.rc.domain.reward.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.ggne.rc.global.entity.BaseEntity;

@Entity
@Table(name = "reward_exchanges")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RewardExchange extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "reward_id", nullable = false)
    private Long rewardId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExchangeStatus status;

    public static RewardExchange create(Long userId, Long rewardId) {
        RewardExchange exchange = new RewardExchange();
        exchange.userId = userId;
        exchange.rewardId = rewardId;
        exchange.status = ExchangeStatus.COMPLETED;

        return exchange;
    }
}
