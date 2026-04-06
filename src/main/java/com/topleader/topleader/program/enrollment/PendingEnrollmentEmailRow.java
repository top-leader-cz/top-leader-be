package com.topleader.topleader.program.enrollment;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record PendingEnrollmentEmailRow(
        Long participantId,
        Long programId,
        String username,
        boolean newUser,
        String firstName,
        String lastName,
        String email,
        String locale,
        String programName,
        String programGoal,
        Integer durationDays,
        Integer sessionsPerParticipant,
        String hrUsername,
        LocalDateTime validFrom,
        String hrFirstName,
        String hrLastName,
        String hrEmail
) {}
