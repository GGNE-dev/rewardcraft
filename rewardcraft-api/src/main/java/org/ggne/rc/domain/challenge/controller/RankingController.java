package org.ggne.rc.domain.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.challenge.service.RankingService;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/{challengeId}/ranking")
public class RankingController {

    private final RankingService rankingService;

    // 상위 N명 랭킹 조회
    @GetMapping("/top")
    public ApiResponse<List<RankingService.RankingEntry>> getTopRanking(
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "100") int limit) {

        return ApiResponse.ok(rankingService.getTopN(challengeId, limit));
    }

    // 내 순위 조회
    @GetMapping("/me")
    public ApiResponse<MyRankResponse> getMyRank(
            @PathVariable Long challengeId,
            @AuthenticationPrincipal Long userId) {

        Long rank = rankingService.getMyRank(challengeId, userId);
        Double score = rankingService.getMyScore(challengeId, userId);

        return ApiResponse.ok(new MyRankResponse(rank, score != null ? score.longValue() : 0L));
    }

    public record MyRankResponse(Long rank, long score) {}
}