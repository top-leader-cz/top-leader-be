package com.topleader.topleader.program.dto;

import com.topleader.topleader.program.ProgramRepository;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public record ProgramDetailDto(
        Long id,
        String name,
        String status,
        ZonedDateTime validFrom,
        ZonedDateTime validTo,
        ZonedDateTime milestoneDate,
        Long daysUntilMilestone,
        ProgramStatsDto stats,
        List<ParticipantDto> participants
) {
    public static ProgramDetailDto from(ProgramRepository.ProgramRow program, ProgramStatsDto stats, List<ParticipantDto> participants) {
        return new ProgramDetailDto(
                program.id(),
                program.name(),
                program.status(),
                toUtc(program.validFrom()),
                toUtc(program.validTo()),
                toUtc(program.milestoneDate()),
                daysUntil(program.milestoneDate()),
                stats,
                participants
        );
    }

    private static Long daysUntil(LocalDateTime date) {
        return date != null ? ChronoUnit.DAYS.between(LocalDateTime.now(), date) : null;
    }

    private static ZonedDateTime toUtc(LocalDateTime ldt) {
        return ldt == null ? null : ldt.atZone(ZoneOffset.UTC);
    }
}
