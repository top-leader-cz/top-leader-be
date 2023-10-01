/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coach-availability")
public class CoachAvailabilityController {

    private final CoachAvailabilityService coachAvailabilityService;

    private final UserRepository userRepository;

    @Secured("COACH")
    @GetMapping("/non-recurring")
    public List<NonReoccurringEventDto> getNonRecurringCoachAvailability(
        @AuthenticationPrincipal UserDetails user,
        @Valid EventFilterDto filterDto
    ) {
        final var userZoneId = userRepository.findById(user.getUsername())
            .map(User::getTimeZone)
            .map(ZoneId::of)
            .orElseThrow();


        final var from = filterDto.from().atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC);
        final var to = filterDto.to().atZone(userZoneId).withZoneSameInstant(ZoneOffset.UTC);

        return coachAvailabilityService.getNonReoccurringByTimeFrame(user.getUsername(), from.toLocalDateTime(), to.toLocalDateTime()).stream()
            .map(e -> new NonReoccurringEventDto(
                e.getDateTimeFrom().atZone(ZoneOffset.UTC).withZoneSameInstant(userZoneId).toLocalDateTime(),
                e.getDateTimeTo().atZone(ZoneOffset.UTC).withZoneSameInstant(userZoneId).toLocalDateTime()
            ))
            .toList();

    }

    @Secured("COACH")
    @PostMapping("/non-recurring")
    public void setNonRecurringCoachAvailability(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetNonrecurringRequestDto request) {

        if (request.events.stream().anyMatch(e -> e.from().isBefore(request.timeFrame().from()))) {
            throw new ApiValidationException("from", "field from cannot by outside of the time frame");
        }

        if (request.events.stream().anyMatch(e -> e.to().isAfter(request.timeFrame().to()))) {
            throw new ApiValidationException("to", "field from cannot by outside of the time frame");
        }

        if (request.events.stream().anyMatch(e -> e.to().isBefore(e.from()))) {
            throw new ApiValidationException("to", "field to cannot be before the field from in a single event");
        }

        coachAvailabilityService.setNonRecurringAvailability(user.getUsername(), request);
    }

    @Secured("COACH")
    @GetMapping("/recurring")
    public List<ReoccurringEventDto> getRecurringCoachAvailability(@AuthenticationPrincipal UserDetails user) {
        final var userZoneId = userRepository.findById(user.getUsername())
            .map(User::getTimeZone)
            .map(ZoneId::of)
            .orElseThrow();

        return coachAvailabilityService.getReoccurring(user.getUsername()).stream()
            .map(e ->
            {
                final var userTimeFrom = LocalDateTime.of(
                    LocalDate.now().with(TemporalAdjusters.next(e.getDayFrom())),
                    e.getTimeFrom()
                ).atZone(ZoneOffset.UTC).withZoneSameInstant(userZoneId);
                final var userTimeTo = LocalDateTime.of(
                    LocalDate.now().with(TemporalAdjusters.next(e.getDayTo())),
                    e.getTimeTo()
                ).atZone(ZoneOffset.UTC).withZoneSameInstant(userZoneId);
                return new ReoccurringEventDto(
                    new ReoccurringEventTimeDto(userTimeFrom.getDayOfWeek(), userTimeFrom.toLocalTime()),
                    new ReoccurringEventTimeDto(userTimeTo.getDayOfWeek(), userTimeTo.toLocalTime())
                );
            })
            .toList();

    }

    @Secured("COACH")
    @PostMapping("/recurring")
    public void setRecurringCoachAvailability(@AuthenticationPrincipal UserDetails user, @RequestBody @NotNull List<ReoccurringEventDto> request) {


        if (
            request.stream().anyMatch(e ->
                e.from().time().isAfter(e.to().time()) && e.from().day().equals(e.to().day())
            )
        ) {
            throw new ApiValidationException("to", "the field to cannot be before the field from in a single day event");
        }
        if (
            request.stream().anyMatch(e ->
                !e.from().day().equals(e.to().day()) && !e.from().day().equals(e.to().day().minus(1))
            )
        ) {
            throw new ApiValidationException("day", "events longer that one day are not supported");
        }

        coachAvailabilityService.setRecurringAvailability(user.getUsername(), request);
    }

    public record EventFilterDto(@NotNull LocalDateTime from, @NotNull LocalDateTime to) {
    }

    public record NonReoccurringEventDto(LocalDateTime from, LocalDateTime to) {
    }

    public record ReoccurringEventDto(ReoccurringEventTimeDto from, ReoccurringEventTimeDto to) {
    }

    public record ReoccurringEventTimeDto(DayOfWeek day, LocalTime time) {
    }

    public record SetNonrecurringRequestDto(
        @NotNull EventFilterDto timeFrame,
        @NotNull List<NonReoccurringEventDto> events
    ) {
    }

}
