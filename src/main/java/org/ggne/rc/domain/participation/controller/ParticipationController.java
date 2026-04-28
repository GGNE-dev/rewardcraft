package org.ggne.rc.domain.participation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.participation.entity.Participation;
import org.ggne.rc.domain.participation.service.ParticipationService;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

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

    public record JoinRequest(
            @NotNull Long userId,
            @NotNull Long challengeId
    ) {}

    public record ParticipationResponse(
            Long id,
            Long userId,
            Long challengeId,
            LocalDateTime joinedAt,
            Long totalPoints
    ) {
        public static ParticipationResponse from(Participation p) {
            return new ParticipationResponse(
                    p.getId(),
                    p.getUser().getId(),
                    p.getChallenge().getId(),
                    p.getJoinedAt(),
                    p.getTotalPoints()
            );
        }
    }
}
