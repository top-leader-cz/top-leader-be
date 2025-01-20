package com.topleader.topleader.calendar.calendly.domain;

import com.topleader.topleader.calendar.domain.CalendarSyncInfo;


import java.time.LocalDateTime;
import java.time.ZonedDateTime;

public record CalendarTokenInfo(
        Boolean active,
        CalendarSyncInfo.Status status,
        ZonedDateTime lastSync) {

    public static final CalendarTokenInfo EMPTY = new CalendarTokenInfo(false, null, null);

}

