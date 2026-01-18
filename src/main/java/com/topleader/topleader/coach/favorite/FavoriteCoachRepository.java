package com.topleader.topleader.coach.favorite;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface FavoriteCoachRepository extends CrudRepository<FavoriteCoach, Long> {

    List<FavoriteCoach> findByUsername(String username);

    @Query("SELECT * FROM favorite_coach WHERE username = :username AND coach_username = :coachUsername")
    Optional<FavoriteCoach> findByUsernameAndCoachUsername(String username, String coachUsername);

    @Modifying
    @Query("DELETE FROM favorite_coach WHERE username = :username AND coach_username = :coachUsername")
    void deleteByUsernameAndCoachUsername(String username, String coachUsername);
}
