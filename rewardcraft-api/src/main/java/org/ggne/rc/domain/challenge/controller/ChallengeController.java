package org.ggne.rc.domain.challenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.ggne.rc.domain.challenge.entity.Challenge;
import org.ggne.rc.domain.challenge.service.ChallengeService;
import org.ggne.rc.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/challenges")
@RequiredArgsConstructor
public class ChallengeController {

    private final ChallengeService challengeService;

    @GetMapping
    public ApiResponse<List<ChallengeResponse>> getActive() {
        return ApiResponse.ok(challengeService.findActive().stream()
                .map(ChallengeResponse::from).toList());
    }

    @GetMapping("/{id}")
    public ApiResponse<ChallengeResponse> getOne(@PathVariable Long id) {
        return ApiResponse.ok(ChallengeResponse.from(challengeService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ChallengeResponse>> create(
            @RequestBody @Valid ChallengeCreateRequest req) {
        Challenge created = challengeService.create(
                req.title(), req.description(), req.startAt(), req.endAt());
        return ResponseEntity.status(201).body(ApiResponse.ok(ChallengeResponse.from(created)));
    }

    public record ChallengeResponse(Long id, String title, String status) {
        public static ChallengeResponse from(Challenge c) {
            return new ChallengeResponse(c.getId(), c.getTitle(), c.getStatus().name());
        }
    }

    public record ChallengeCreateRequest(
            @NotBlank String title,
            String description,
            @NotNull LocalDateTime startAt,
            @NotNull LocalDateTime endAt
    ) {}
}
