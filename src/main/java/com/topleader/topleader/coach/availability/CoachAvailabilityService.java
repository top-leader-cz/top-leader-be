/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.topleader.topleader.calendar.domain.SyncEvent;
import com.topleader.topleader.calendar.calendly.CalendlyService;
import com.topleader.topleader.calendar.google.GoogleCalendarService;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
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

    private final CalendlyService calendlyService;

    private final UserRepository userRepository;

    public List<CoachAvailability> getNonReoccurringByTimeFrame(String username, LocalDateTime from, LocalDateTime to) {
        return coachAvailabilityRepository.findAllByUsernameAndDateTimeFromAfterAndDateTimeToBefore(username, from, to);
    }

    public List<CoachAvailability> getReoccurring(String username) {
        return coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username);
    }

    public List<LocalDateTime> getAvailabilitySplitIntoHoursFiltered(String username, LocalDateTime from, LocalDateTime to, Boolean testFreeBusy) {
        var events = getSyncEvents(username, from, to, testFreeBusy);

        return getAvailabilitySplitIntoHours(username, from, to).stream()
                .filter(isNotInsideOf(events))
                .collect(Collectors.toList());
    }

    public List<SyncEvent> getSyncEvents(String username, LocalDateTime from, LocalDateTime to, Boolean testFreeBusy) {
        final var googleEvents = googleCalendarService.getUserEvents(username, from, to, testFreeBusy);
        var calendlyEvents = calendlyService.getUserEvents(username, from, to);

        return Stream.concat(googleEvents.stream(), calendlyEvents.stream())
                .collect(Collectors.toList());
    }

    private static Predicate<LocalDateTime> isNotInsideOf(List<SyncEvent> googleEvents) {
        return time -> googleEvents.stream().noneMatch(isInsideOf(time));
    }

    private static Predicate<SyncEvent> isInsideOf(LocalDateTime startTime) {
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

    public List<LocalDateTime> getAvailabilitySplitIntoHours(String username, LocalDateTime from, LocalDateTime to) {
        var requestedIntervals = toIntervals(from, to);

        var available = Stream.concat(
                getReoccurring(username).stream()
                        .map(e -> {
                            var timeFrom = LocalDateTime.of(from.toLocalDate().with(TemporalAdjusters.nextOrSame(e.getDayFrom())), e.getTimeFrom());
                            var timeTo = LocalDateTime.of(from.toLocalDate().with(TemporalAdjusters.next(e.getDayTo())), e.getTimeTo());
                            return toIntervals(timeFrom, timeTo);
                        })
                        .flatMap(Collection::stream),

                getNonReoccurringByTimeFrame(username, from, to).stream()
                        .map(a -> toIntervals(a.getDateTimeFrom(), a.getDateTimeTo()))
        ).collect(Collectors.toSet());

        requestedIntervals.removeIf(a -> !available.contains(a));

        return requestedIntervals.stream()
                .sorted()
                .toList();
    }

    Set<LocalDateTime> toIntervals(LocalDateTime from, LocalDateTime to) {
        var intervals = new HashSet<LocalDateTime>();
        var current = from;
        while (!current.isAfter(to)) {
            intervals.add(current);
            current = current.plusHours(1); // Increment by 1 hour
        }
        return intervals;
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
}
