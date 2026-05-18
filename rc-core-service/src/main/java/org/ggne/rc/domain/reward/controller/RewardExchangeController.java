package org.ggne.rc.domain.reward.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.reward.entity.Reward;
import org.ggne.rc.domain.reward.entity.RewardExchange;
import org.ggne.rc.domain.reward.repository.RewardExchangeRepository;
import org.ggne.rc.domain.reward.repository.RewardRepository;
import org.ggne.rc.domain.reward.service.RewardExchangeService;
import org.ggne.rc.global.exception.ErrorCode;
import org.ggne.rc.global.exception.RCBusinessException;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardExchangeController {

    private final RewardExchangeService rewardExchangeService;
    private final RewardRepository rewardRepository;
    private final RewardExchangeRepository rewardExchangeRepository;

    @GetMapping
    public ApiResponse<List<RewardResponse>> getRewards() {
        return ApiResponse.ok(rewardRepository.findAll().stream().map(RewardResponse::from).toList());
    }

    @PostMapping("/{rewardId}/exchange")
    public ApiResponse<RewardExchangeService.RewardExchangeResult> exchange(
            @PathVariable Long rewardId,
            @AuthenticationPrincipal Long userId) {
        return ApiResponse.ok(rewardExchangeService.exchange(userId, rewardId));
    }

    // 내 교환 내역 조회
    @GetMapping("/my-exchanges")
    public ApiResponse<List<MyExchangeResponse>> getMyExchanges(
            @AuthenticationPrincipal Long userId) {
        List<MyExchangeResponse> result = rewardExchangeRepository
                .findByUserIdOrderByCreatedAtDesc(userId).stream()
                .map(e -> {
                    String name = rewardRepository.findById(e.getRewardId())
                            .map(Reward::getName).orElse("삭제된 리워드");
                    return new MyExchangeResponse(e.getId(), name, e.getRewardId(),
                            e.getStatus().name(),
                            e.getCreatedAt() != null ? e.getCreatedAt().toString() : null);
                })
                .toList();
        return ApiResponse.ok(result);
    }

    // 관리자: 새 리워드 등록
    @PostMapping
    @PreAuthorize("hasAuthority('PERM_REWARD_MANAGE')")
    @Transactional
    public ResponseEntity<ApiResponse<RewardResponse>> createReward(
            @RequestBody @Valid CreateRewardRequest req) {
        Reward reward = Reward.builder()
                .name(req.name())
                .requiredPoints(req.requiredPoints())
                .totalStock(req.stock())
                .build();
        return ResponseEntity.status(201).body(ApiResponse.ok(RewardResponse.from(rewardRepository.save(reward))));
    }

    // 관리자: 재고 조정 (delta 양수 = 추가, 음수 = 차감)
    @PatchMapping("/{id}/stock")
    @PreAuthorize("hasAuthority('PERM_REWARD_MANAGE')")
    @Transactional
    public ApiResponse<RewardResponse> adjustStock(
            @PathVariable Long id,
            @RequestBody @Valid StockAdjustRequest req) {
        Reward reward = rewardRepository.findById(id)
                .orElseThrow(() -> new RCBusinessException(ErrorCode.REWARD_NOT_FOUND));
        reward.adjustStock(req.delta());
        return ApiResponse.ok(RewardResponse.from(reward));
    }

    public record RewardResponse(Long id, String name, Long requiredPoints, Long totalStock, Long remainingStock) {
        public static RewardResponse from(Reward r) {
            return new RewardResponse(r.getId(), r.getName(), r.getRequiredPoints(), r.getTotalStock(), r.getRemainingStock());
        }
    }

    public record CreateRewardRequest(
            @NotBlank String name,
            @NotNull @Positive Long requiredPoints,
            @NotNull @Positive Long stock
    ) {}

    public record StockAdjustRequest(@NotNull long delta) {}

    public record MyExchangeResponse(
            Long exchangeId, String rewardName, Long rewardId,
            String status, String exchangedAt
    ) {}
}
