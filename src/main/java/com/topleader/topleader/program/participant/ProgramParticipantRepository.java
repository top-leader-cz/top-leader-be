package com.topleader.topleader.program.participant;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface ProgramParticipantRepository extends ListCrudRepository<ProgramParticipant, Long> {

    List<ProgramParticipant> findByProgramId(Long programId);

    @Query("""
            SELECT pp.* FROM program_participant pp
            JOIN program p ON p.id = pp.program_id
            WHERE pp.username = :username
              AND p.status IN ('CREATED', 'ACTIVE')
            ORDER BY p.created_at DESC
            LIMIT 1
            """)
    Optional<ProgramParticipant> findActiveByUsername(String username);

    @Query("SELECT * FROM program_participant WHERE program_id = :programId AND username = :username")
    Optional<ProgramParticipant> findByProgramIdAndUsername(Long programId, String username);

    @Modifying
    @Query("DELETE FROM program_participant WHERE program_id = :programId AND username = :username")
    void deleteByProgramIdAndUsername(Long programId, String username);

    @Query("""
            SELECT pp.username
            FROM program_participant pp
            JOIN program p ON p.id = pp.program_id
            WHERE pp.username IN (:usernames)
              AND p.status IN ('CREATED', 'ACTIVE')
              AND (CAST(:excludeProgramId AS bigint) IS NULL OR pp.program_id != :excludeProgramId)
            """)
    List<String> findUsernamesInActivePrograms(Collection<String> usernames, Long excludeProgramId);
}
