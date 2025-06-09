package com.topleader.topleader.user.badge;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BadgeRepository extends JpaRepository<Badge, Badge.BadgeId> {

    @Query("select b from Badge b where b.username = :username and b.year = :year")
    List<Badge> getUserBadges(String username, int year);
}
