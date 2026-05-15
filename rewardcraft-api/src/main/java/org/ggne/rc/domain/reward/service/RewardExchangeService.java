package org.ggne.rc.domain.reward.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ggne.rc.domain.participation.repository.ParticipationRepository;
import org.ggne.rc.domain.reward.entity.Reward;
import org.ggne.rc.domain.reward.entity.RewardExchange;
import org.ggne.rc.domain.reward.repository.RewardExchangeRepository;
import org.ggne.rc.domain.reward.repository.RewardRepository;
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
    private final ParticipationRepository participationRepository;
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

            // 락 획득 후 트랜잭션 시작 — @Transactional 자기 호출 문제를 TransactionTemplate으로 해결
            // 트랜잭션이 완전히 커밋된 뒤 finally에서 락이 해제됨
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
        // 1. DB 비관적 락으로 재고 행 잠금 (SELECT ... FOR UPDATE)
        Reward reward = rewardRepository.findByIdForUpdate(rewardId)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.REWARD_NOT_FOUND));

        // 2. 재고 검증
        if (reward.getRemainingStock() <= 0) {
            throw new RCBusinessException(ErrorCode.OUT_OF_STOCK);
        }

        // 3. 사용자 보유 포인트 검증
        Long totalPoints = participationRepository.sumTotalPointsByUserId(userId);
        if (totalPoints < reward.getRequiredPoints()) {
            throw new RCBusinessException(ErrorCode.INSUFFICIENT_POINTS);
        }

        // 4. 재고 차감 (더티 체킹으로 자동 UPDATE)
        reward.decreaseStock();

        // 5. 교환 이력 저장
        RewardExchange exchange = RewardExchange.create(userId, rewardId);
        exchangeRepository.save(exchange);

        return new RewardExchangeResult(exchange.getId(), reward.getRemainingStock());
    }

    public record RewardExchangeResult(Long exchangeId, Long remainingStock) {}
}
