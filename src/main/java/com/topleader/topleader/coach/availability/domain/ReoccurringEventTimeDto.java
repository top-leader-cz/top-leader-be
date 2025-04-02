package com.topleader.topleader.coach.availability.domain;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ReoccurringEventTimeDto(DayOfWeek day, LocalTime time) {
}