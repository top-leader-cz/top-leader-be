/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.util.common;

import java.time.LocalDateTime;
import java.time.ZoneId;
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
}
