package com.topleader.topleader.user.badge;


import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

import static com.topleader.topleader.user.badge.Badge.AchievementType.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/latest/user-badges")
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public BadgeResponse getBadges(@AuthenticationPrincipal UserDetails auth) {
        var username = auth.getUsername();
        var now = LocalDateTime.now();
        var currentMonth = now.getMonth();

        var userBadges = badgeService.getBadges(username);

       var byMonth = userBadges.stream()
                .collect(Collectors.groupingBy(Badge::getMonth));

        var badges = Arrays.stream(Month.values())
                .takeWhile(m -> m.compareTo(currentMonth) <= 0)
                .collect(Collectors.toMap(
                        m -> m,
                        m -> byMonth.getOrDefault(m, List.of()).size() == 3,
                        (a,b) -> a,
                        LinkedHashMap::new
                ));

        var thisMonth = byMonth.getOrDefault(currentMonth, List.of());

        return new BadgeResponse()
                .setCompletedSession(
                        thisMonth.stream()
                                .anyMatch(b -> b.getAchievementType() == COMPLETE_SESSION)
                )
                .setCompletedShortTermGoal(
                        thisMonth.stream()
                                .anyMatch(b -> b.getAchievementType() == COMPLETED_SHORT_GOAL)
                )
                .setWatchedVideo(
                        thisMonth.stream()
                                .anyMatch(b -> b.getAchievementType() == WATCHED_VIDEO)
                )
                .setBadges(badges);
    }

    @PostMapping("watched-video")
    public void watchedVideo(@AuthenticationPrincipal UserDetails auth) {
        badgeService.recordAchievement(auth.getUsername(), WATCHED_VIDEO);
    }
}
