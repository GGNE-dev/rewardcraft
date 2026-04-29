package org.ggne.rc.domain.participation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.participation.entity.Participation;
import org.ggne.rc.domain.participation.service.ParticipationService;
import org.ggne.rc.global.response.ApiResponse;
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


    // [N+1 데모용 — 비교 후 제거]
    @GetMapping("/n-plus-one-demo/{challengeId}")
    public ApiResponse<List<String>> nPlusOneDemo(@PathVariable Long challengeId) {
        return ApiResponse.ok(
                participationService.getParticipantNicknamesWithNPlusOne(challengeId)
        );
    }

    // [Fetch Join 데모용 — 비교 후 제거]
    @GetMapping("/fetch-join-demo/{challengeId}")
    public ApiResponse<List<String>> fetchJoinDemo(@PathVariable Long challengeId) {
        return ApiResponse.ok(
                participationService.getParticipantNicknamesWithFetchJoin(challengeId)
        );
    }



    public record JoinRequest(
            @NotNull Long userId,
            @NotNull Long challengeId
    ) {}

    public record ParticipationResponse(
            Long id,
            Long userId,
            Long challengeId,
            Long totalPoints
    ) {
        public static ParticipationResponse from(Participation p) {
            return new ParticipationResponse(
                    p.getId(),
                    p.getUser().getId(),
                    p.getChallenge().getId(),
                    p.getTotalPoints()
            );
        }
    }
}
