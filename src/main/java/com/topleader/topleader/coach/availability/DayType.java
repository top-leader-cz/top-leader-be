/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import lombok.Getter;


/**
 * @author Daniel Slavik
 */
@Getter
public enum DayType {
    MONDAY(0),
    TUESDAY(1),

    WEDNESDAY(2),
    THURSDAY(3),
    FRIDAY(4),
    SATURDAY(5),
    SUNDAY(6)
    ;

    private final int dayOffset;

    DayType(int dayOffset) {
        this.dayOffset = dayOffset;
    }

}
