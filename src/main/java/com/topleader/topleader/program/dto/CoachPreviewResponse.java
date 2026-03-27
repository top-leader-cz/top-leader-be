package com.topleader.topleader.program.dto;

import java.util.List;

// Response wrapper with metadata for wizard summary
// exact = matched both language + category hard filters
// recommended = AI-suggested from language-only pool to fill target
public record CoachPreviewResponse(
        int total,
        int exact,
        int recommended,
        String summary,
        List<CoachPreviewDto> coaches
) {}
