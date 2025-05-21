package com.topleader.topleader.user.badge;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;

    public void recordAchievement(String username, Badge.AchievementType type) {
        var now = LocalDate.now();
        badgeRepository.save(new Badge().setBadgeId(new Badge.BadgeId(username, type, now.getMonth(), now.getYear())));
    }


    public List<Badge> getBadges(String username) {
       return badgeRepository.getUerBadges(username,  LocalDate.now().getYear());
    }
}
