package com.topleader.topleader.coach.availability.domain;

public record ReoccurringEventDto(ReoccurringEventTimeDto from, ReoccurringEventTimeDto to) {
    public static ReoccurringEventDto EMPTY = new ReoccurringEventDto(null, null);
}
