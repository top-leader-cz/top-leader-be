package com.topleader.topleader.coach.note;


import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface CoachUserNoteRepository extends CrudRepository<CoachUserNote, Long>, PagingAndSortingRepository<CoachUserNote, Long> {

    @Query("SELECT * FROM coach_user_note WHERE coach_username = :coachUsername AND username = :username")
    Optional<CoachUserNote> findByCoachUsernameAndUsername(String coachUsername, String username);
}
