package com.topleader.topleader.program.manager;

public record ManagerParticipantRow(
        Long programId,
        String programName,
        String username,
        String firstName,
        String lastName,
        String enrollmentStatus,
        int attendanceCount,
        double practiceCompletionRate
) {}
