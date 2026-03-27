package com.topleader.topleader.program.participant.assessment;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface AssessmentResponseRepository extends ListCrudRepository<AssessmentResponse, Long> {

    @Query("SELECT * FROM assessment_response WHERE participant_id = :participantId ORDER BY cycle, type")
    List<AssessmentResponse> findByParticipantId(Long participantId);

    @Query("SELECT * FROM assessment_response WHERE participant_id = :participantId AND type = :type AND cycle = :cycle")
    Optional<AssessmentResponse> findByParticipantIdAndTypeAndCycle(Long participantId, String type, int cycle);

    default Optional<AssessmentResponse> findByParticipantIdAndTypeAndCycle(Long participantId, AssessmentResponse.Type type, int cycle) {
        return findByParticipantIdAndTypeAndCycle(participantId, type.name(), cycle);
    }
}
