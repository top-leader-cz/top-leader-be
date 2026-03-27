package com.topleader.topleader.program.dto;

import com.topleader.topleader.program.ProgramRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ProgramSummaryDto(
        Long id,
        String name,
        String status,
        LocalDateTime validFrom,
        LocalDateTime validTo,
        LocalDateTime milestoneDate,
        Long daysUntilMilestone,
        int totalParticipants,
        int activeParticipants
) {
    public static ProgramSummaryDto from(ProgramRepository.ProgramSummaryRow row) {
        return new ProgramSummaryDto(
                row.id(),
                row.name(),
                row.status(),
                row.validFrom(),
                row.validTo(),
                row.milestoneDate(),
                daysUntil(row.milestoneDate()),
                row.totalParticipants(),
                row.activeParticipants()
        );
    }

    private static Long daysUntil(LocalDateTime date) {
        return date != null ? ChronoUnit.DAYS.between(LocalDateTime.now(), date) : null;
    }
}
