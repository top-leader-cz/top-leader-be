package com.topleader.topleader.program.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RecommendCoachesRequest(
        @NotBlank String goal,
        @NotNull Set<String> focusAreas,
        String targetGroup
) {}
