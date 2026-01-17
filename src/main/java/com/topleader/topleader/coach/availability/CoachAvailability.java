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

    private DayOfWeek dayFrom;

    private DayOfWeek dayTo;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private Boolean recurring;

    private LocalDateTime dateTimeFrom;

    private LocalDateTime dateTimeTo;


}
