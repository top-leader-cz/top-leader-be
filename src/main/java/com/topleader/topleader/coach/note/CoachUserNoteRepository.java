package com.topleader.topleader.coach.note;


import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CoachUserNoteRepository extends JpaRepository<CoachUserNote, Long> {

    Optional<CoachUserNote> findByCoachIdAndUserId(String coachId, String userId);
}
