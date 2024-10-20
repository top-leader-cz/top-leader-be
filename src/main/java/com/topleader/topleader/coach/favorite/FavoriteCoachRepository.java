package com.topleader.topleader.coach.favorite;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FavoriteCoachRepository extends JpaRepository<FavoriteCoach, FavoriteCoach.FavoriteCoachId> {

    List<FavoriteCoach> findByUsername(String username);

    void deleteById(FavoriteCoach.FavoriteCoachId id);
}
