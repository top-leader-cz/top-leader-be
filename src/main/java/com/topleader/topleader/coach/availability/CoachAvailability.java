/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Table("coach_availability")
@Accessors(chain = true)
@NoArgsConstructor
public class CoachAvailability {

    @Id
    private Long id;

    private String username;

    private String dayFrom;

    private String dayTo;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private Boolean recurring;

    private LocalDateTime dateTimeFrom;

    private LocalDateTime dateTimeTo;

    public DayOfWeek getDayFromEnum() {
        return dayFrom != null ? DayOfWeek.valueOf(dayFrom) : null;
    }

    public CoachAvailability setDayFromEnum(DayOfWeek dayFrom) {
        this.dayFrom = dayFrom != null ? dayFrom.name() : null;
        return this;
    }

    public CoachAvailability setDayFrom(DayOfWeek dayFrom) {
        return setDayFromEnum(dayFrom);
    }

    public DayOfWeek getDayToEnum() {
        return dayTo != null ? DayOfWeek.valueOf(dayTo) : null;
    }

    public CoachAvailability setDayToEnum(DayOfWeek dayTo) {
        this.dayTo = dayTo != null ? dayTo.name() : null;
        return this;
    }

    public CoachAvailability setDayTo(DayOfWeek dayTo) {
        return setDayToEnum(dayTo);
    }

}
