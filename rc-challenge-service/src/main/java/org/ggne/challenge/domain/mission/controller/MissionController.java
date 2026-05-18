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
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    // 사용자: 미션 완료 신청 (PENDING 저장)
    @PostMapping("/api/participations/{participationId}/missions")
    public ResponseEntity<ApiResponse<MissionResponse>> complete(
            @PathVariable Long participationId,
            @RequestBody @Valid CompleteRequest req) {
        MissionLog log = missionService.complete(participationId, req.points(), req.memo());
        return ResponseEntity.status(201).body(ApiResponse.ok(MissionResponse.from(log)));
    }

    // 사용자: 참여별 미션 내역 조회
    @GetMapping("/api/participations/{participationId}/missions")
    public ApiResponse<List<MissionResponse>> list(@PathVariable Long participationId) {
        return ApiResponse.ok(missionService.findByParticipationId(participationId).stream()
                .map(MissionResponse::from).toList());
    }

    // 운영자/관리자: 전체 승인 대기 목록
    @GetMapping("/api/missions/pending")
    public ApiResponse<List<PendingMissionResponse>> listPending() {
        return ApiResponse.ok(missionService.findAllPending().stream()
                .map(PendingMissionResponse::from).toList());
    }

    // 운영자/관리자: 미션 승인
    @PatchMapping("/api/missions/{id}/approve")
    public ApiResponse<MissionResponse> approve(@PathVariable Long id) {
        return ApiResponse.ok(MissionResponse.from(missionService.approve(id)));
    }

    // 운영자/관리자: 미션 거절
    @PatchMapping("/api/missions/{id}/reject")
    public ApiResponse<MissionResponse> reject(@PathVariable Long id) {
        return ApiResponse.ok(MissionResponse.from(missionService.reject(id)));
    }

    public record CompleteRequest(@NotNull @Min(1) Long points, String memo) {}

    public record MissionResponse(
            Long id, Long participationId,
            LocalDateTime completedAt, Long pointsEarned, String memo, String status
    ) {
        public static MissionResponse from(MissionLog log) {
            return new MissionResponse(
                    log.getId(), log.getParticipation().getId(),
                    log.getCompletedAt(), log.getPointsEarned(), log.getMemo(),
                    log.getStatus().name());
        }
    }

    public record PendingMissionResponse(
            Long id, Long participationId, Long userId,
            Long challengeId, String challengeTitle,
            Long pointsEarned, String memo, LocalDateTime submittedAt
    ) {
        public static PendingMissionResponse from(MissionLog log) {
            return new PendingMissionResponse(
                    log.getId(),
                    log.getParticipation().getId(),
                    log.getParticipation().getUserId(),
                    log.getParticipation().getChallenge().getId(),
                    log.getParticipation().getChallenge().getTitle(),
                    log.getPointsEarned(),
                    log.getMemo(),
                    log.getCompletedAt());
        }
    }
}
