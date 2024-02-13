package com.topleader.topleader.user.userinsight;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/latest/user-insight")
@RequiredArgsConstructor
public class UserInsightController {

    public final UserInsightService userInsightService;


    @GetMapping
    public UserInsightDto setValues(@AuthenticationPrincipal UserDetails user) {
        var userInsight = userInsightService.getInsight(user.getUsername()).orElseThrow();
        return new UserInsightDto(userInsight.getLeadershipStyleAnalysis(), userInsight.getAnimalSpiritGuide());
    }

    public record UserInsightDto(String leaderShipStyleAnalysis, String animalSpiritGuide) {
    }
}
