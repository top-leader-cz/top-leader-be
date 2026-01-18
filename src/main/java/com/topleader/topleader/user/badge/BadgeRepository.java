package com.topleader.topleader.user.badge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Month;
import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends JpaRepository<Badge, Long> {

    @Query("select b from Badge b where b.username = :username and b.year = :year")
    List<Badge> getUserBadges(String username, int year);

    Optional<Badge> findByUsernameAndAchievementTypeAndMonthAndYear(
            String username, Badge.AchievementType achievementType, Month month, Integer year);
}
