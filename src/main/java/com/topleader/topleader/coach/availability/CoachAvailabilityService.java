/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.topleader.topleader.google.GoogleCalendarService;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.util.common.user.UserUtils.getUserTimeZoneId;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class CoachAvailabilityService {

    private final CoachAvailabilityRepository coachAvailabilityRepository;

    private final GoogleCalendarService googleCalendarService;

    private final UserRepository userRepository;

    public List<CoachAvailability> getNonReoccurringByTimeFrame(String username, LocalDateTime from, LocalDateTime to) {
        return coachAvailabilityRepository.findAllByUsernameAndDateTimeFromAfterAndDateTimeToBefore(username, from, to);
    }

    public List<CoachAvailability> getReoccurring(String username) {
        return coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username);
    }

    public Set<LocalDateTime> getAvailabilitySplitIntoHoursFiltered(String username, LocalDateTime from, LocalDateTime to, Boolean testFreeBusy) {

        final var googleEvents = googleCalendarService.getUserEvents(username, from, to, testFreeBusy);

        if (googleEvents.isEmpty()) {
            return getAvailabilitySplitIntoHours(username, from, to);
        }

        return getAvailabilitySplitIntoHours(username, from, to).stream()
            .filter(isNotInsideOf(googleEvents))
            .collect(Collectors.toSet());
    }

    private static Predicate<LocalDateTime> isNotInsideOf(List<GoogleCalendarService.SyncEvent> googleEvents) {
        return time -> googleEvents.stream().noneMatch(isInsideOf(time));
    }

    private static Predicate<GoogleCalendarService.SyncEvent> isInsideOf(LocalDateTime startTime) {
        return googleEvent -> {
            final var endTime = startTime.plusMinutes(59);
            final var startTimeWithBuffer = startTime.plusMinutes(1);

            if (startTimeWithBuffer.isBefore(googleEvent.startDate()) && endTime.isBefore(googleEvent.startDate())) {
                return false;
            }

            if (startTimeWithBuffer.isAfter(googleEvent.endDate()) && endTime.isAfter(googleEvent.endDate())) {
                return false;
            }
            return true;
        };
    }

    public Set<LocalDateTime> getAvailabilitySplitIntoHours(String username, LocalDateTime from, LocalDateTime to) {

        final var dayMap = toDayMap(from, to);

        return processData(
            Stream.concat(
                getReoccurring(username).stream()
                    .filter(e -> {
                            if (!dayMap.containsKey(e.getDayFrom()) && !dayMap.containsKey(e.getDayTo())) {
                                return false;
                            }

                            if (dayMap.containsKey(e.getDayFrom()) && (LocalDateTime.of(dayMap.get(e.getDayFrom()), e.getTimeFrom()).isAfter(to))) {
                                return false;
                            }

                            return !dayMap.containsKey(e.getDayTo()) || (!LocalDateTime.of(dayMap.get(e.getDayTo()), e.getTimeTo()).isBefore(from));
                        }
                    )
                    .map(a -> new TimeRange(
                        toDateTime(a.getDayFrom(), a.getTimeFrom(), dayMap, from, from::isAfter),
                        toDateTime(a.getDayTo(), a.getTimeTo(), dayMap, to, to::isBefore)
                    ))
                ,
                getNonReoccurringByTimeFrame(username, from, to).stream()
                    .map(a -> new TimeRange(a.getDateTimeFrom(), a.getDateTimeTo()))
            )
        );

    }

    private static LocalDateTime toDateTime(
        DayOfWeek day,
        LocalTime time,
        Map<DayOfWeek, LocalDate> dayMap,
        LocalDateTime orDefault,
        Predicate<LocalDateTime> shouldReturnDefault
    ) {

        if (!dayMap.containsKey(day)) {
            return orDefault;
        }

        final var possibleResult = LocalDateTime.of(dayMap.get(day), time);

        if (shouldReturnDefault.test(possibleResult)) {
            return orDefault;
        }

        return possibleResult;
    }

    private Set<LocalDateTime> processData(Stream<TimeRange> stream) {
        final var dateCache = new HashSet<LocalDateTime>();

        return stream
            .flatMap(a -> splitTimeRangeByHour(a).stream()
                .filter(timeRange -> {
                    if (dateCache.contains(timeRange.timeFrom())) {
                        return false;
                    }
                    dateCache.add(timeRange.timeFrom());
                    return true;
                })
                .map(TimeRange::timeFrom)
            )
            .collect(Collectors.toSet());
    }

    private static List<TimeRange> splitTimeRangeByHour(TimeRange timeRange) {

        final var hourlyIntervals = new ArrayList<TimeRange>();

        var currentInterval = timeRange.timeFrom();

        while (!currentInterval.isAfter(timeRange.timeTo()) && !currentInterval.equals(timeRange.timeTo())) {

            final var nextInterval = currentInterval.plusHours(1);
            hourlyIntervals.add(new TimeRange(currentInterval, nextInterval.isBefore(timeRange.timeTo()) ? nextInterval : timeRange.timeTo()));
            currentInterval = nextInterval;
        }

        return hourlyIntervals;
    }

    @Transactional
    public void setNonRecurringAvailability(String username, CoachAvailabilityController.SetNonrecurringRequestDto request) {

        final var userZoneId = getUserTimeZoneId(userRepository.findById(username));

        final var from = request.timeFrame().from()
            .atZone(userZoneId)
            .withZoneSameInstant(ZoneOffset.UTC);

        final var to = request.timeFrame().to()
            .atZone(userZoneId)
            .withZoneSameInstant(ZoneOffset.UTC);

        final var shiftedEvents = request.events().stream()
            .map(e ->
                new CoachAvailabilityController.NonReoccurringEventDto(
                    e.from()
                        .atZone(userZoneId)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime(),
                    e.to()
                        .atZone(userZoneId)
                        .withZoneSameInstant(ZoneOffset.UTC)
                        .toLocalDateTime()
                )
            )
            .collect(Collectors.toSet());

        final var existingEvents = getNonReoccurringByTimeFrame(username, from.toLocalDateTime(), to.toLocalDateTime());

        final var toDelete = existingEvents.stream()
            .filter(e -> !shiftedEvents.contains(new CoachAvailabilityController.NonReoccurringEventDto(e.getDateTimeFrom(), e.getDateTimeTo())))
            .toList();

        final var existingEventsMapped = existingEvents.stream()
            .map(e -> new CoachAvailabilityController.NonReoccurringEventDto(e.getDateTimeFrom(), e.getDateTimeTo()))
            .collect(Collectors.toSet());

        final var toCreate = shiftedEvents.stream()
            .filter(e -> !existingEventsMapped.contains(e))
            .toList();

        if (!toDelete.isEmpty()) {
            coachAvailabilityRepository.deleteAll(toDelete);
        }

        if (!toCreate.isEmpty()) {
            coachAvailabilityRepository.saveAll(toCreate.stream()
                .map(e -> new CoachAvailability()
                    .setUsername(username)
                    .setRecurring(false)
                    .setDateTimeFrom(e.from())
                    .setDateTimeTo(e.to())
                )
                .toList()
            );
        }
    }

    @Transactional
    public void setRecurringAvailability(String username, List<CoachAvailabilityController.ReoccurringEventDto> events) {
        final var userZoneId = getUserTimeZoneId(userRepository.findById(username));

        final var shiftedEvents = events.stream()
            .map(e -> {
                final var userTimeFrom = LocalDateTime.of(
                    LocalDate.now().with(TemporalAdjusters.next(e.from().day())),
                    e.from().time()
                ).atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC);
                final var userTimeTo = LocalDateTime.of(
                    LocalDate.now().with(TemporalAdjusters.next(e.to().day())),
                    e.to().time()
                ).atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC);
                return new CoachAvailabilityController.ReoccurringEventDto(
                    new CoachAvailabilityController.ReoccurringEventTimeDto(userTimeFrom.getDayOfWeek(), userTimeFrom.toLocalTime()),
                    new CoachAvailabilityController.ReoccurringEventTimeDto(userTimeTo.getDayOfWeek(), userTimeTo.toLocalTime())
                );
            })
            .collect(Collectors.toSet());

        final var existing = getReoccurring(username);

        final var existingMapped = existing.stream()
            .map(e -> new CoachAvailabilityController.ReoccurringEventDto(
                new CoachAvailabilityController.ReoccurringEventTimeDto(e.getDayFrom(), e.getTimeFrom()),
                new CoachAvailabilityController.ReoccurringEventTimeDto(e.getDayTo(), e.getTimeTo())
            ))
            .collect(Collectors.toSet());

        final var toDelete = existing.stream()
            .filter(e -> !shiftedEvents.contains(
                new CoachAvailabilityController.ReoccurringEventDto(
                    new CoachAvailabilityController.ReoccurringEventTimeDto(e.getDayFrom(), e.getTimeFrom()),
                    new CoachAvailabilityController.ReoccurringEventTimeDto(e.getDayTo(), e.getTimeTo())
                )
            ))
            .toList();

        final var toCreate = shiftedEvents.stream()
            .filter(e -> !existingMapped.contains(e))
            .toList();

        if (!toDelete.isEmpty()) {
            coachAvailabilityRepository.deleteAll(toDelete);
        }

        if (!toCreate.isEmpty()) {
            coachAvailabilityRepository.saveAll(toCreate.stream()
                .map(e -> new CoachAvailability()
                    .setUsername(username)
                    .setRecurring(true)
                    .setDayFrom(e.from().day())
                    .setTimeFrom(e.from().time())
                    .setDayTo(e.to().day())
                    .setTimeTo(e.to().time())
                )
                .toList()
            );
        }

    }

    private static Map<DayOfWeek, LocalDate> toDayMap(LocalDateTime from, LocalDateTime to) {
        final var result = new EnumMap<DayOfWeek, LocalDate>(DayOfWeek.class);

        result.put(from.getDayOfWeek(), from.toLocalDate());

        var temDate = from.plusDays(1);

        while (temDate.isBefore(to)) {
            result.put(temDate.getDayOfWeek(), temDate.toLocalDate());
            temDate = temDate.plusDays(1);
        }

        result.put(to.getDayOfWeek(), to.toLocalDate());

        return result;
    }

    private record TimeRange(LocalDateTime timeFrom, LocalDateTime timeTo) {
    }
}
