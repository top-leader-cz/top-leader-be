/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.topleader.topleader.common.calendar.domain.SyncEvent;
import com.topleader.topleader.common.calendar.calendly.CalendlyService;
import com.topleader.topleader.common.calendar.google.GoogleCalendarService;
import com.topleader.topleader.coach.availability.domain.NonReoccurringEventDto;
import com.topleader.topleader.coach.availability.domain.ReoccurringEventDto;
import com.topleader.topleader.coach.availability.domain.ReoccurringEventTimeDto;
import com.topleader.topleader.coach.availability.settings.AvailabilitySettingRepository;
import com.topleader.topleader.coach.availability.settings.CoachAvailabilitySettings;
import com.topleader.topleader.user.UserRepository;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.common.calendar.domain.CalendarSyncInfo.SyncType.CALENDLY;
import static com.topleader.topleader.common.util.common.user.UserUtils.getUserTimeZoneId;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class CoachAvailabilityService {
    private static final int NOT_ALLOWED_BOOK = 24;

    private final CoachAvailabilityRepository coachAvailabilityRepository;

    private final GoogleCalendarService googleCalendarService;

    private final CalendlyService calendlyService;

    private final UserRepository userRepository;

    private final AvailabilitySettingRepository availabilitySettingRepository;

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public List<CoachAvailability> getNonReoccurringByTimeFrame(String username, LocalDateTime from, LocalDateTime to) {
        return coachAvailabilityRepository.findAllByUsernameAndDateTimeFromAfterAndDateTimeToBefore(username, from, to);
    }

    public List<CoachAvailability> getReoccurring(String username) {
        return coachAvailabilityRepository.findAllByUsernameAndRecurringIsTrue(username);
    }

    public List<LocalDateTime> getAvailabilitySplitIntoHoursFiltered(String username, LocalDateTime to, Boolean testFreeBusy) {
        var from24Hour = LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(NOT_ALLOWED_BOOK);
        var events = getSyncEvents(username, from24Hour, to, testFreeBusy);

        return getAvailabilitySplitIntoHours(username, from24Hour, to).stream()
                .filter(isNotInsideOf(events))
                .toList();
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
        Multimap<DayOfWeek, CoachAvailability> reoccurringMap = ArrayListMultimap.create();
        getReoccurringEvents(username).forEach(e -> reoccurringMap.put(e.getDayFrom(), e));
        var nonReoccurring = getNonReoccurringByTimeFrame(username, from, to);

        return toIntervals(from, to).stream()
                .filter(r -> {
                    var availabilities = reoccurringMap.get(r.getDayOfWeek());
                    return testNonReoccurring(r, nonReoccurring) || availabilities.stream()
                            .anyMatch(e -> {
                                if (e != null && e.getTimeFrom() != null && e.getTimeTo() != null) {
                                    var timeFrom = LocalDateTime.of(r.toLocalDate().with(TemporalAdjusters.nextOrSame(e.getDayFrom())), e.getTimeFrom());
                                    var timeTo = LocalDateTime.of(r.toLocalDate().with(TemporalAdjusters.nextOrSame(e.getDayTo())), e.getTimeTo());
                                    return testNonReoccurring(r, nonReoccurring) || isInRange(r, timeFrom, timeTo);
                                }
                                return false;
                            });
                })
                .collect(Collectors.toSet())
                .stream()
                .sorted()
                .toList();
    }

    private List<CoachAvailability> getReoccurringEvents(String username) {
        return availabilitySettingRepository.findByCoach(username)
                .filter(CoachAvailabilitySettings::isActive)
                .map(a -> {
                    if (CALENDLY == a.getType()) {
                        return calendlyService.getEventAvailability(username, a.getResource()).stream()
                                .map(e -> new CoachAvailability()
                                        .setDayFrom(e.from().day())
                                        .setTimeFrom(e.from().time())
                                        .setDayTo(e.to().day())
                                        .setTimeTo(e.to().time()))
                                .toList();
                    }
                    return null;
                }).orElse(getReoccurring(username));
    }

    private boolean testNonReoccurring(LocalDateTime requested, List<CoachAvailability> nonReoccurring) {
        return nonReoccurring.stream()
                .anyMatch(n -> isInRange(requested, n.getDateTimeFrom(), n.getDateTimeTo()));
    }

    private boolean isInRange(LocalDateTime requested, LocalDateTime from, LocalDateTime to) {
        final var endTime = requested.plusMinutes(59);
        return (requested.isAfter(from) || requested.isEqual(from)) &&
                (endTime.isBefore(to) || endTime.isEqual(to));
    }

    List<LocalDateTime> toIntervals(LocalDateTime from, LocalDateTime to) {
        var intervals = new ArrayList<LocalDateTime>();
        var current = from;
        while (!current.isAfter(to)) {
            intervals.add(current);
            current = current.plusHours(1); // Increment by 1 hour
        }
        return intervals;
    }

    @Transactional
    public void setNonRecurringAvailability(String username, CoachAvailabilityController.SetNonrecurringRequestDto request) {
        var timeFrame = request.timeFrame();

        if(timeFrame.from() == null || timeFrame.to() == null) {
            return;
        }

        final var userZoneId = getUserTimeZoneId(userRepository.findByUsername(username));

        final var from = request.timeFrame().from()
            .atZone(userZoneId)
            .withZoneSameInstant(ZoneOffset.UTC);

        final var to = request.timeFrame().to()
            .atZone(userZoneId)
            .withZoneSameInstant(ZoneOffset.UTC);

        final var shiftedEvents = request.events().stream()
            .map(e ->
                new NonReoccurringEventDto(
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
            .filter(e -> !shiftedEvents.contains(new NonReoccurringEventDto(e.getDateTimeFrom(), e.getDateTimeTo())))
            .toList();

        final var existingEventsMapped = existingEvents.stream()
            .map(e -> new NonReoccurringEventDto(e.getDateTimeFrom(), e.getDateTimeTo()))
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
    public void setRecurringAvailability(String username, List<ReoccurringEventDto> events) {
        final var userZoneId = getUserTimeZoneId(userRepository.findByUsername(username));

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
                return new ReoccurringEventDto(
                    new ReoccurringEventTimeDto(userTimeFrom.getDayOfWeek(), userTimeFrom.toLocalTime()),
                    new ReoccurringEventTimeDto(userTimeTo.getDayOfWeek(), userTimeTo.toLocalTime())
                );
            })
            .collect(Collectors.toSet());

        final var existing = getReoccurring(username);

        final var existingMapped = existing.stream()
            .map(e -> new ReoccurringEventDto(
                new ReoccurringEventTimeDto(e.getDayFrom(), e.getTimeFrom()),
                new ReoccurringEventTimeDto(e.getDayTo(), e.getTimeTo())
            ))
            .collect(Collectors.toSet());

        final var toDelete = existing.stream()
            .filter(e -> !shiftedEvents.contains(
                new ReoccurringEventDto(
                    new ReoccurringEventTimeDto(e.getDayFrom(), e.getTimeFrom()),
                    new ReoccurringEventTimeDto(e.getDayTo(), e.getTimeTo())
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
