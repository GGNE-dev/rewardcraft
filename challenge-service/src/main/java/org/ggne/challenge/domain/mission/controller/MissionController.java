package org.ggne.challenge.domain.mission.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.mission.entity.MissionLog;
import org.ggne.challenge.domain.mission.service.MissionService;
import org.ggne.challenge.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/participations/{participationId}/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @PostMapping
    public ResponseEntity<ApiResponse<MissionResponse>> complete(
            @PathVariable Long participationId,
            @RequestBody @Valid CompleteRequest req) {
        MissionLog log = missionService.complete(participationId, req.points(), req.memo());
        return ResponseEntity.status(201).body(ApiResponse.ok(MissionResponse.from(log)));
    }

    @GetMapping
    public ApiResponse<List<MissionResponse>> list(@PathVariable Long participationId) {
        return ApiResponse.ok(missionService.findByParticipationId(participationId).stream()
                .map(MissionResponse::from).toList());
    }

    public record CompleteRequest(@NotNull @Min(1) Long points, String memo) {}

    public record MissionResponse(
            Long id, Long participationId,
            LocalDateTime completedAt, Long pointsEarned, String memo
    ) {
        public static MissionResponse from(MissionLog log) {
            return new MissionResponse(
                    log.getId(), log.getParticipation().getId(),
                    log.getCompletedAt(), log.getPointsEarned(), log.getMemo());
        }
    }
}
