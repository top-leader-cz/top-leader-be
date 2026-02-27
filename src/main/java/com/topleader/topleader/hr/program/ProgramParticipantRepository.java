package com.topleader.topleader.hr.program;

import org.springframework.data.repository.ListCrudRepository;

import java.util.Optional;

public interface ProgramParticipantRepository extends ListCrudRepository<ProgramParticipant, Long> {

    Optional<ProgramParticipant> findByProgramIdAndUsername(Long programId, String username);
}
