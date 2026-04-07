package com.topleader.topleader.program.recommendation;

public record RecommendationDto(
        String type,
        String title,
        String url,
        String duration,
        String thumbnailUrl
) {}
