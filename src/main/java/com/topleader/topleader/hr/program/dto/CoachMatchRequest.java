package com.topleader.topleader.hr.program.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record CoachMatchRequest(
        @NotNull Set<String> coachLanguages,
        @NotNull Set<String> coachCategories,
        int participantCount,
        @NotBlank String goal,
        @NotNull Set<String> focusAreas,
        String targetGroup
) {}
