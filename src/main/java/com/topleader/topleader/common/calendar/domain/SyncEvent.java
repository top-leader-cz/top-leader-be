package com.topleader.topleader.common.calendar.domain;



import java.time.LocalDateTime;

public record SyncEvent(String username, LocalDateTime startDate, LocalDateTime endDate) {
}