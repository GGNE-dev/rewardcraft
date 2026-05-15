package org.ggne.challenge.domain.participation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.participation.dto.ParticipantSummaryDto;
import org.ggne.challenge.domain.participation.entity.Participation;
import org.ggne.challenge.domain.participation.service.ParticipationService;
import org.ggne.challenge.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/participations")
@RequiredArgsConstructor
public class ParticipationController {

    private final ParticipationService participationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ParticipationResponse>> join(
            @RequestBody @Valid JoinRequest req) {
        Participation p = participationService.join(req.userId(), req.challengeId());
        return ResponseEntity.status(201).body(ApiResponse.ok(ParticipationResponse.from(p)));
    }

    // rewardcraft-api의 GET /api/users/{id}/participations를 대체
    @GetMapping
    public ApiResponse<List<ParticipationResponse>> getByUser(@RequestParam Long userId) {
        return ApiResponse.ok(participationService.findByUserId(userId).stream()
                .map(ParticipationResponse::from).toList());
    }

    @GetMapping("/{challengeId}/summary")
    public ApiResponse<List<ParticipantSummaryDto>> getParticipantSummary(
            @PathVariable Long challengeId) {
        return ApiResponse.ok(participationService.getParticipantSummary(challengeId));
    }

    public record JoinRequest(
            @NotNull Long userId,
            @NotNull Long challengeId
    ) {}

    public record ParticipationResponse(Long id, Long userId, Long challengeId, Long totalPoints) {
        public static ParticipationResponse from(Participation p) {
            return new ParticipationResponse(
                    p.getId(), p.getUserId(), p.getChallenge().getId(), p.getTotalPoints());
        }
    }
}
