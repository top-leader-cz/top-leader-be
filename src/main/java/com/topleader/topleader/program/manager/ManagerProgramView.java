package com.topleader.topleader.program.manager;

import java.util.List;

public record ManagerProgramView(
        Long programId,
        String programName,
        List<ManagerParticipantView> participants
) {
    public record ManagerParticipantView(
            String username,
            String firstName,
            String lastName,
            String status,
            int attendanceCount,
            double practiceCompletionRate
    ) {}
}
