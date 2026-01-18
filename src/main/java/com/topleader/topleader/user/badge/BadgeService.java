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
        var month = now.getMonth();
        var year = now.getYear();

        // Only save if badge doesn't already exist for this user/type/month/year
        badgeRepository.findByUsernameAndAchievementTypeAndMonthAndYear(username, type, month, year)
                .ifPresentOrElse(
                        existing -> {}, // Badge already exists, do nothing
                        () -> badgeRepository.save(new Badge()
                                .setUsername(username)
                                .setAchievementType(type)
                                .setMonth(month)
                                .setYear(year))
                );
    }


    public List<Badge> getBadges(String username) {
       return badgeRepository.getUserBadges(username,  LocalDate.now().getYear());
    }
}
