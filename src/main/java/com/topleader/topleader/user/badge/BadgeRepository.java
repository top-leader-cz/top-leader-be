package com.topleader.topleader.user.badge;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface BadgeRepository extends ListCrudRepository<Badge, Long> {

    @Query("SELECT * FROM badge WHERE username = :username AND year = :year")
    List<Badge> getUserBadges(String username, int year);

    @Query("SELECT * FROM badge WHERE username = :username AND achievement_type = :achievementType AND month = :month AND year = :year")
    Optional<Badge> findByUsernameAndAchievementTypeAndMonthAndYear(String username, String achievementType, String month, Integer year);
}
