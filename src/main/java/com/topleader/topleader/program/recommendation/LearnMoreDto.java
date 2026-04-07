package com.topleader.topleader.program.recommendation;

import java.util.List;

public record LearnMoreDto(
        String focusAreaLabel,
        List<RecommendationDto> articles,
        List<RecommendationDto> videos
) {
    public boolean isEmpty() {
        return articles.isEmpty() && videos.isEmpty();
    }
}
