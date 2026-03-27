package com.topleader.topleader.program.participant.practice;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface WeeklyPracticeRepository extends ListCrudRepository<WeeklyPractice, Long> {

    @Query("SELECT * FROM weekly_practice WHERE participant_id = :participantId AND cycle = :cycle AND week_number = :weekNumber")
    Optional<WeeklyPractice> findByParticipantIdAndCycleAndWeekNumber(Long participantId, int cycle, int weekNumber);

    @Query("SELECT * FROM weekly_practice WHERE participant_id = :participantId ORDER BY cycle DESC, week_number DESC LIMIT 1")
    Optional<WeeklyPractice> findLatestByParticipantId(Long participantId);

    @Query("SELECT COUNT(*) FROM weekly_practice WHERE participant_id = :participantId")
    long countByParticipantId(Long participantId);

    @Query("SELECT COUNT(*) FROM weekly_practice WHERE participant_id = :participantId AND friday_response IS NOT NULL")
    long countRespondedByParticipantId(Long participantId);

    @Query("SELECT COUNT(*) FROM weekly_practice WHERE participant_id = :participantId AND cycle = :cycle")
    long countByParticipantIdAndCycle(Long participantId, int cycle);

    @Query("SELECT COUNT(*) FROM weekly_practice WHERE participant_id = :participantId AND cycle = :cycle AND friday_response IS NOT NULL")
    long countRespondedByParticipantIdAndCycle(Long participantId, int cycle);
}
