package org.ggne.rc.domain.reward.controller;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.reward.service.RewardExchangeService;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/rewards")
public class RewardExchangeController {

    private final RewardExchangeService rewardExchangeService;

    @PostMapping("/{rewardId}/exchange")
    public ApiResponse<RewardExchangeService.RewardExchangeResult> exchange(
            @PathVariable Long rewardId,
            @AuthenticationPrincipal Long userId) {

        return ApiResponse.ok(rewardExchangeService.exchange(userId, rewardId));
    }
}
