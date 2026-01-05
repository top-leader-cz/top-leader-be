/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.util.common;

import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.model.EventDateTime;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;


/**
 * @author Daniel Slavik
 */
public final class DateUtils {
    private DateUtils() {
        //util class
    }

    public static Date toDate(LocalDateTime dateToConvert) {
        return java.util.Date
            .from(dateToConvert.atZone(ZoneId.systemDefault())
                .toInstant());
    }

    public static LocalDateTime convertToDateTime(EventDateTime eventDateTime) {
        DateTime dateTime = eventDateTime.getDateTime();
        if (dateTime == null) {
            // If dateTime is null, use the date value
            dateTime = eventDateTime.getDate();
        }
        if (dateTime == null) {
            throw new IllegalArgumentException("EventDateTime does not contain date or dateTime");
        }
        Instant instant = Instant.ofEpochMilli(dateTime.getValue());
        return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC)
            .toLocalDateTime();
    }

    public static DateTime convertToDateTime(LocalDateTime eventDateTime) {
        return new DateTime(eventDateTime.atZone(ZoneOffset.UTC).toInstant().toEpochMilli());
    }

    public static LocalDateTime convertToDateTime(DateTime eventDateTime) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(eventDateTime.getValue()), ZoneOffset.UTC);
    }
}
