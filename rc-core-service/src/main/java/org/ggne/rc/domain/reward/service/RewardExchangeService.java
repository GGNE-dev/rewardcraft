package org.ggne.rc.domain.reward.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.domain.reward.entity.Reward;
import org.ggne.rc.domain.reward.entity.RewardExchange;
import org.ggne.rc.domain.reward.repository.RewardExchangeRepository;
import org.ggne.rc.domain.reward.repository.RewardRepository;
import org.ggne.rc.global.client.ChallengeClient;
import org.ggne.rc.global.exception.ErrorCode;
import org.ggne.rc.global.exception.RCBusinessException;
import org.ggne.rc.global.exception.RCSystemException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RewardExchangeService {

    private final RedissonClient redissonClient;
    private final RewardRepository rewardRepository;
    private final RewardExchangeRepository exchangeRepository;
    private final ChallengeClient challengeClient;   // Feign: challenge-service 포인트 조회
    private final PlatformTransactionManager transactionManager;

    private static final String LOCK_KEY = "lock:reward:%d";

    public RewardExchangeResult exchange(Long userId, Long rewardId) {
        String lockKey = String.format(LOCK_KEY, rewardId);
        RLock lock = redissonClient.getLock(lockKey);

        boolean locked = false;
        try {
            locked = lock.tryLock(3, 5, TimeUnit.SECONDS);
            if (!locked) {
                throw new RCBusinessException(ErrorCode.LOCK_ACQUISITION_FAILED);
            }

            TransactionTemplate tx = new TransactionTemplate(transactionManager);
            return tx.execute(status -> processExchange(userId, rewardId));

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RCSystemException("분산 락 획득 중 인터럽트 발생", e);
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private RewardExchangeResult processExchange(Long userId, Long rewardId) {
        Reward reward = rewardRepository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.REWARD_NOT_FOUND));

        if (reward.getRemainingStock() <= 0) {
            throw new RCBusinessException(ErrorCode.OUT_OF_STOCK);
        }

        // challenge-service에서 사용자 포인트 조회 (Feign + CircuitBreaker)
        // challenge-service 장애 시 ChallengeClientFallback이 CHALLENGE_SERVICE_UNAVAILABLE 예외를 던짐
        Long totalPoints = challengeClient.getUserTotalPoints(userId);
        if (totalPoints < reward.getRequiredPoints()) {
            throw new RCBusinessException(ErrorCode.INSUFFICIENT_POINTS);
        }

        reward.decreaseStock();

        RewardExchange exchange = RewardExchange.create(userId, rewardId);
        exchangeRepository.save(exchange);

        return new RewardExchangeResult(exchange.getId(), reward.getRemainingStock());
    }

    public record RewardExchangeResult(Long exchangeId, Long remainingStock) {}
}
