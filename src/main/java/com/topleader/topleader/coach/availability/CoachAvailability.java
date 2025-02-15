/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;


/**
 * @author Daniel Slavik
 */
@Getter
@Setter
@ToString
@Entity
@Accessors(chain = true)
@NoArgsConstructor
public class CoachAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "coach_availability_seq")
    @SequenceGenerator(name = "coach_availability_seq", sequenceName = "coach_availability_seq", allocationSize = 1)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayFrom;

    @Enumerated(EnumType.STRING)
    private DayOfWeek dayTo;

    private LocalTime timeFrom;

    private LocalTime timeTo;

    private Boolean recurring;

    private LocalDateTime dateTimeFrom;

    private LocalDateTime dateTimeTo;

    private AvailabilityType type;

    public enum AvailabilityType {
        LOCAL,
        GOOGLE,
        CALENDLY,
    }

}
