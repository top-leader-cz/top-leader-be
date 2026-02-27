package com.topleader.topleader.hr.program.participant;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.List;
import java.util.Optional;

public interface ProgramParticipantRepository extends ListCrudRepository<ProgramParticipant, Long> {

    List<ProgramParticipant> findByProgramId(Long programId);

    @Modifying
    @Query("DELETE FROM program_participant WHERE program_id = :programId AND username = :username")
    void deleteByProgramIdAndUsername(Long programId, String username);
}
