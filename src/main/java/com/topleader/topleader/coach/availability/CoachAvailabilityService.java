/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class CoachAvailabilityService {

    private final CoachAvailabilityRepository coachAvailabilityRepository;

    public List<CoachAvailability> getWeekAvailabilitySplitIntoHours(String username, LocalDate dateFrom, DayType day) {

        return processData(
            Stream.concat(
                coachAvailabilityRepository.findAllByUsernameAndDayAndRecurringIsTrue(username, day).stream()
                    .map(a -> calculateDayFrom(a, dateFrom)),
                coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndDayAndRecurringIsFalse(username, dateFrom, day).stream()
            ),
            dateFrom
        );

    }

    public List<CoachAvailability> getWeekAvailabilitySplitIntoHours(String username, LocalDate dateFrom) {

        return processData(
            Stream.concat(
                coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username).stream()
                    .map(a -> calculateDayFrom(a, dateFrom)),
                coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(username, dateFrom).stream()
            ),
            dateFrom
        );
    }

    private List<CoachAvailability> processData(Stream<CoachAvailability> stream, LocalDate dateFrom) {
        final var dateCache = new HashSet<String>();

        return stream
            .flatMap(a -> splitTimeRangeByHour(a.getTimeFrom(), a.getTimeTo()).stream()
                .filter(timeRange -> {
                    final var timeMark = String.join("#", a.getDay().name(), timeRange.timeFrom().toString());
                    if (dateCache.contains(timeMark)) {
                        return false;
                    }
                    dateCache.add(timeMark);
                    return true;
                })
                .map(timeRange -> new CoachAvailability()
                    .setFirstDayOfTheWeek(dateFrom)
                    .setDate(a.getDate())
                    .setDay(a.getDay())
                    .setRecurring(a.getRecurring())
                    .setUsername(a.getUsername())
                    .setTimeFrom(timeRange.timeFrom())
                    .setTimeTo(timeRange.timeTo())
                )
            )
            .toList();
    }

    private CoachAvailability calculateDayFrom(CoachAvailability a, LocalDate dateFrom) {
        return a.setDate(dateFrom.plusDays(a.getDay().getDayOffset()));
    }

    private static List<TimeRange> splitTimeRangeByHour(LocalTime timeFrom, LocalTime timeTo) {
        final var hourlyIntervals = new ArrayList<TimeRange>();

        LocalTime currentInterval = timeFrom;

        while (!currentInterval.isAfter(timeTo) && !currentInterval.equals(timeTo)) {

            final var nextInterval = currentInterval.plusHours(1);
            hourlyIntervals.add(new TimeRange(currentInterval, nextInterval.isBefore(timeTo) ? nextInterval : timeTo));
            currentInterval = nextInterval;
        }

        return hourlyIntervals;
    }


    public List<CoachAvailability> getWeekAvailability(
        String username,
        LocalDate dateFrom,
        AvailabilityType type
    ) {
        return switch (type) {
            case RECURRING -> coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username);
            case NON_RECURRING -> coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(username, dateFrom);
            case ALL -> Stream.concat(
                coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username).stream(),
                coachAvailabilityRepository.findAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(username, dateFrom).stream()
            ).toList();
        };
    }

    @Transactional
    public List<CoachAvailability> setWeekAvailability(
        String username,
        LocalDate dateFrom,
        List<CoachAvailability> availabilities
    ) {
        coachAvailabilityRepository.deleteAllByUsernameAndFirstDayOfTheWeekAndRecurringIsFalse(username, dateFrom);
        return coachAvailabilityRepository.saveAll(availabilities);
    }

    @Transactional
    public List<CoachAvailability> setRecurringAvailability(
        String username,
        List<CoachAvailability> availabilities
    ) {
        coachAvailabilityRepository.deleteAllByUsernameAndRecurringIsTrue(username);
        return coachAvailabilityRepository.saveAll(availabilities);
    }

    private record TimeRange(LocalTime timeFrom, LocalTime timeTo) {
    }
}
