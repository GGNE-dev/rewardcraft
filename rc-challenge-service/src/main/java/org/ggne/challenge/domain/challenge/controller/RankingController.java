package org.ggne.challenge.domain.challenge.controller;

import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.challenge.service.RankingService;
import org.ggne.challenge.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/challenges/{challengeId}/ranking")
public class RankingController {

    private final RankingService rankingService;

    @GetMapping("/top")
    public ApiResponse<List<RankingService.RankingEntry>> getTopRanking(
            @PathVariable Long challengeId,
            @RequestParam(defaultValue = "100") int limit) {
        return ApiResponse.ok(rankingService.getTopN(challengeId, limit));
    }

    // challenge-service에는 JWT 인증이 없으므로 userId를 파라미터로 받음
    // 실제 운영에서는 API Gateway가 JWT를 검증하고 헤더로 userId를 전달
    @GetMapping("/me")
    public ApiResponse<MyRankResponse> getMyRank(
            @PathVariable Long challengeId,
            @RequestParam Long userId) {
        Long rank = rankingService.getMyRank(challengeId, userId);
        Double score = rankingService.getMyScore(challengeId, userId);
        return ApiResponse.ok(new MyRankResponse(rank, score != null ? score.longValue() : 0L));
    }

    public record MyRankResponse(Long rank, long score) {}
}
