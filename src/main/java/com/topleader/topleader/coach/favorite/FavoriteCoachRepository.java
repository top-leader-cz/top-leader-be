package com.topleader.topleader.coach.favorite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import java.util.List;

public interface FavoriteCoachRepository extends JpaRepository<FavoriteCoach, Long> {

    List<FavoriteCoach> findByUsername(String username);

    @Modifying
    void deleteByUsernameAndCoachUsername(String username, String coachUsername);
}
