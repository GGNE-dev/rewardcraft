package org.ggne.challenge.domain.participation.controller;

import lombok.RequiredArgsConstructor;
import org.ggne.challenge.domain.participation.service.ParticipationService;
import org.springframework.web.bind.annotation.*;

// rewardcraft-api가 Feign으로 호출하는 내부 전용 API
// 운영 환경에서는 외부 트래픽이 접근 불가한 내부 네트워크(또는 API Gateway 필터)로 보호
@RestController
@RequestMapping("/internal/participants")
@RequiredArgsConstructor
public class ParticipationInternalController {

    private final ParticipationService participationService;

    @GetMapping("/points")
    public Long getUserTotalPoints(@RequestParam Long userId) {
        return participationService.getUserTotalPoints(userId);
    }

    @PostMapping("/points/deduct")
    public void deductPoints(@RequestParam Long userId, @RequestParam long points) {
        participationService.deductPoints(userId, points);
    }
}
