/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.topleader.topleader.exception.ApiValidationException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static java.util.Objects.isNull;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coach-availability")
public class CoachAvailabilityController {

    private static final String DATE_PARAM_NAME = "firstDayOfTheWeek";

    private final CoachAvailabilityService coachAvailabilityService;

    @Secured("COACH")
    @GetMapping("/{type}")
    public Map<DayType, List<CoachAvailabilityDto>> getCoachAvailability(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable AvailabilityType type,
        @RequestParam(required = false, name = DATE_PARAM_NAME) LocalDate firstDayOfTheWeek
    ) {
        if (isNull(firstDayOfTheWeek) && !type.equals(AvailabilityType.RECURRING)) {
            throw new ApiValidationException(DATE_PARAM_NAME, "Param is mandatory for type " + type);
        }

        return coachAvailabilityService.getWeekAvailability(user.getUsername(), firstDayOfTheWeek, type).stream()
            .map(a -> Map.entry(a.getDay(), CoachAvailabilityDto.from(a)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
    }

    @Secured("COACH")
    @PostMapping("/RECURRING")
    public Map<DayType, List<CoachAvailabilityDto>> getCoachAvailabilityRecurring(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody SetCoachAvailabilityRequestDto request
    ) {
        if (!isNull(request.firstDayOfTheWeek())) {
            throw new ApiValidationException(DATE_PARAM_NAME, "Param is not supported for type RECURRING");
        }

        return coachAvailabilityService.setRecurringAvailability(
                user.getUsername(),
                request.availabilities.entrySet().stream()
                    .flatMap(e -> e.getValue().stream()
                        .map(a -> a.toRecurring(user.getUsername(), e.getKey()))
                    )
                    .toList()
            ).stream()
            .map(a -> Map.entry(a.getDay(), CoachAvailabilityDto.from(a)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
    }

    @Secured("COACH")
    @PostMapping("/NON_RECURRING")
    public Map<DayType, List<CoachAvailabilityDto>> getCoachAvailabilityNonRecurring(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody SetCoachAvailabilityRequestDto request
    ) {
        if (isNull(request.firstDayOfTheWeek())) {
            throw new ApiValidationException(DATE_PARAM_NAME, "Param is mandatory for type NON_RECURRING");
        }

        return coachAvailabilityService.setWeekAvailability(
                user.getUsername(),
                request.firstDayOfTheWeek(),
                request.availabilities.entrySet().stream()
                    .flatMap(e -> e.getValue().stream()
                        .map(a -> a.toNonRecurring(user.getUsername(), e.getKey(), request.firstDayOfTheWeek()))
                    )
                    .toList()
            ).stream()
            .map(a -> Map.entry(a.getDay(), CoachAvailabilityDto.from(a)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
    }

    public record CoachAvailabilityDto(
        DayType day,
        LocalDate date,
        LocalTime timeFrom,
        LocalTime timeTo,
        Boolean recurring
    ) {
        public static CoachAvailabilityDto from(CoachAvailability c) {
            return new CoachAvailabilityDto(
                c.getDay(),
                c.getDate(),
                c.getTimeFrom(),
                c.getTimeTo(),
                c.getRecurring()
            );
        }
    }

    public record SetCoachAvailabilityRecurringDto(
        LocalTime timeFrom,
        LocalTime timeTo
    ) {
        public CoachAvailability toRecurring(String username, DayType day) {
            return new CoachAvailability()
                .setUsername(username)
                .setDay(day)
                .setRecurring(true)
                .setTimeFrom(timeFrom)
                .setTimeTo(timeTo);
        }
        public CoachAvailability toNonRecurring(String username, DayType day, LocalDate firstDayOfWeek) {
            return new CoachAvailability()
                .setUsername(username)
                .setDay(day)
                .setFirstDayOfTheWeek(firstDayOfWeek)
                .setDate(firstDayOfWeek.plusDays(day.getDayOffset()))
                .setRecurring(false)
                .setTimeFrom(timeFrom)
                .setTimeTo(timeTo);
        }


    }

    public record SetCoachAvailabilityRequestDto(
        LocalDate firstDayOfTheWeek,
        Map<DayType, List<SetCoachAvailabilityRecurringDto>> availabilities

    ) {

    }
}
