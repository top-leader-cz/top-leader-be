package com.topleader.topleader.hr.program.dto;

import com.topleader.topleader.hr.program.Program;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public record ProgramDraftDto(
        Long id,
        String name,
        String goal,
        String targetGroup,
        String status,
        Integer durationDays,
        Integer cycleLengthDays,
        List<CheckpointDto> checkpoints,
        LocalDateTime milestoneDate,
        Set<String> focusAreas,
        Integer sessionsPerParticipant,
        String recommendedCadence,
        Program.CoachAssignmentModel coachAssignmentModel,
        Set<String> shortlistedCoaches,
        boolean microActionsEnabled,
        Set<String> enabledOptions,
        Set<String> coachLanguages,
        Set<String> coachCategories
) {}
