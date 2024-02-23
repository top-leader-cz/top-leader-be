package com.topleader.topleader.user.userinsight;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/latest/user-insight")
@RequiredArgsConstructor
public class UserInsightController {

    public final UserInsightService userInsightService;


    @GetMapping
    public Map<String, InsightItem> getInsight(@AuthenticationPrincipal UserDetails user) {
        var userInsight = userInsightService.getInsight(user.getUsername()).orElse(new UserInsight());
        return Map.of(
                "leaderShipStyle", new InsightItem(userInsight.getLeadershipStyleAnalysis(), false),
                "animalSpirit", new InsightItem(userInsight.getAnimalSpiritGuide(), false),
                "leadershipTip", new InsightItem(userInsight.getLeadershipTip(), false),
                "personalGrowthTip", new InsightItem(userInsight.getPersonalGrowthTip(), false)
        );
    }

    @GetMapping("/generate-tips")
    public void generateTips(@AuthenticationPrincipal UserDetails user) {
        userInsightService.generateTips(user.getUsername());
    }


    public record InsightItem(String text, boolean isPending) {
    }
}
