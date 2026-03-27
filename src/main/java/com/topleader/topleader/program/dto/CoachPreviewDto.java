package com.topleader.topleader.program.dto;

import java.util.Set;

public record CoachPreviewDto(
        String username, String firstName, String lastName,
        String bio, Set<String> languages, Set<String> fields,
        String reason, String matchType
) {}
