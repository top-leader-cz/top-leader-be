package com.topleader.topleader.hr.program.dto;

import com.topleader.topleader.hr.program.ProgramRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record ProgramDetailDto(
        Long id,
        String name,
        String status,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        LocalDateTime milestoneDate,
        Long daysUntilMilestone,
        ProgramStatsDto stats,
        List<ParticipantDto> participants
) {
    public static ProgramDetailDto from(ProgramRepository.ProgramRow program, ProgramStatsDto stats, List<ParticipantDto> participants) {
        return new ProgramDetailDto(
                program.id(),
                program.name(),
                program.status(),
                program.validFrom(),
                program.validTo(),
                program.milestoneDate(),
                daysUntil(program.milestoneDate()),
                stats,
                participants
        );
    }

    private static Long daysUntil(LocalDateTime date) {
        return date != null ? ChronoUnit.DAYS.between(LocalDateTime.now(), date) : null;
    }
}
