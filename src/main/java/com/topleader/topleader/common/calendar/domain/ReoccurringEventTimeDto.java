package com.topleader.topleader.common.calendar.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ReoccurringEventTimeDto(DayOfWeek day, LocalTime time) {
}
