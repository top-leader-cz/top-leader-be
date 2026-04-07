package com.topleader.topleader.program.recommendation;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;

public interface ProgramParticipantRecommendationRepository
        extends ListCrudRepository<ProgramParticipantRecommendation, Long> {

    @Query("""
            SELECT * FROM program_participant_recommendation
            WHERE program_participant_id = :participantId AND cycle = :cycle
            ORDER BY type ASC, relevance_rank ASC
            """)
    List<ProgramParticipantRecommendation> findByParticipantAndCycle(Long participantId, Integer cycle);

    @Modifying
    @Query("""
            DELETE FROM program_participant_recommendation
            WHERE program_participant_id = :participantId AND cycle = :cycle
            """)
    void deleteByParticipantAndCycle(Long participantId, Integer cycle);
}
