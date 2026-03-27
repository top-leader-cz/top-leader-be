package com.topleader.topleader.program.dto;

import com.topleader.topleader.program.Program;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record SaveProgramRequest(
        Long id,
        @NotBlank String name,
        String goal,
        String targetGroup,
        int durationDays,
        Integer cycleLengthDays,
        LocalDateTime startDate,
        LocalDateTime milestoneDate,
        @NotNull Set<String> focusAreas,
        @NotNull List<ParticipantAssignment> participants,
        int sessionsPerParticipant,
        String recommendedCadence,
        Program.CoachAssignmentModel coachAssignmentModel,
        @NotNull Set<String> shortlistedCoaches,
        boolean microActionsEnabled,
        @NotNull Set<String> enabledOptions,
        @NotNull Set<String> coachLanguages,
        @NotNull Set<String> coachCategories
) {}
