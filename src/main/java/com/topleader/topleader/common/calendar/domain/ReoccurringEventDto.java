package com.topleader.topleader.common.calendar.domain;

public record ReoccurringEventDto(ReoccurringEventTimeDto from, ReoccurringEventTimeDto to) {
    public static ReoccurringEventDto EMPTY = new ReoccurringEventDto(null, null);
}
