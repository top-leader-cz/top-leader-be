package com.topleader.topleader.program.dto;

import com.topleader.topleader.program.ProgramRepository;
import com.topleader.topleader.program.ProgramService;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public record ParticipantDto(
        String username,
        String firstName,
        String lastName,
        String coachUsername,
        String managerUsername,
        LocalDateTime lastActiveAt,
        int sessionsConsumed,
        int sessionsAllocated,
        ParticipantStatus status,
        String enrollmentStatus
) {
    public enum ParticipantStatus {
        NOT_STARTED, ON_TRACK, AT_RISK
    }

    public static ParticipantDto from(ProgramRepository.ParticipantRow row, LocalDateTime validFrom, LocalDateTime milestoneDate) {
        return new ParticipantDto(
                row.username(),
                row.firstName(),
                row.lastName(),
                row.coachUsername(),
                row.managerUsername(),
                row.lastLoginAt(),
                row.consumedUnits(),
                row.allocatedUnits(),
                computeStatus(row, validFrom, milestoneDate),
                row.enrollmentStatus()
        );
    }

    private static ParticipantStatus computeStatus(ProgramRepository.ParticipantRow row, LocalDateTime validFrom, LocalDateTime milestoneDate) {
        var totalDays = validFrom != null && milestoneDate != null
                ? ChronoUnit.DAYS.between(validFrom, milestoneDate)
                : 0L;
        var daysElapsed = totalDays > 0
                ? Math.min(ChronoUnit.DAYS.between(validFrom, LocalDateTime.now()), totalDays)
                : 0L;
        if (daysElapsed <= 0) {
            return ParticipantStatus.NOT_STARTED;
        }
        var threshold = row.allocatedUnits() * ((double) daysElapsed / totalDays) * ProgramService.AT_RISK_GRACE_BUFFER;
        return row.consumedUnits() < threshold ? ParticipantStatus.AT_RISK : ParticipantStatus.ON_TRACK;
    }
}
