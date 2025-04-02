/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.availability;

import com.topleader.topleader.calendar.calendly.CalendlyService;
import com.topleader.topleader.calendar.calendly.domain.EventType;
import com.topleader.topleader.calendar.domain.CalendarSyncInfo;
import com.topleader.topleader.coach.availability.domain.NonReoccurringEventDto;
import com.topleader.topleader.coach.availability.domain.ReoccurringEventDto;
import com.topleader.topleader.coach.availability.domain.ReoccurringEventTimeDto;
import com.topleader.topleader.coach.availability.settings.AvailabilitySettingRepository;
import com.topleader.topleader.coach.availability.settings.CoachAvailabilitySettings;
import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import static com.topleader.topleader.exception.ErrorCodeConstants.FIELD_OUTSIDE_OF_FRAME;
import static com.topleader.topleader.exception.ErrorCodeConstants.MORE_THEN_24_EVENT;
import static com.topleader.topleader.util.common.user.UserUtils.getUserTimeZoneId;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coach-availability")
public class CoachAvailabilityController {

    private final CoachAvailabilityService coachAvailabilityService;

    private final UserRepository userRepository;

    private final CalendlyService calendlyService;

    private final AvailabilitySettingRepository availabilitySettingRepository;

    @Secured("COACH")
    @GetMapping("/non-recurring")
    public List<NonReoccurringEventDto> getNonRecurringCoachAvailability(
        @AuthenticationPrincipal UserDetails user,
        @Valid EventFilterDto filterDto
    ) {
        final var userZoneId = getUserTimeZoneId(userRepository.findById(user.getUsername()));

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
            throw new ApiValidationException(FIELD_OUTSIDE_OF_FRAME, "from", request.timeFrame().from().toString() ,"field from cannot by outside of the time frame");
        }

        if (request.events.stream().anyMatch(e -> e.to().isAfter(request.timeFrame().to()))) {
            throw new ApiValidationException(FIELD_OUTSIDE_OF_FRAME, "to", request.timeFrame().to().toString() ,"field from cannot by outside of the time frame");
        }

        if (request.events.stream().anyMatch(e -> e.to().isBefore(e.from()))) {
            throw new ApiValidationException(FIELD_OUTSIDE_OF_FRAME, "to", request.timeFrame().to().toString() ,"field to cannot be before the field from in a single event");
        }

        coachAvailabilityService.setNonRecurringAvailability(user.getUsername(), request);
    }

    @Secured("COACH")
    @GetMapping("/recurring")
    public List<ReoccurringEventDto> getRecurringCoachAvailability(@AuthenticationPrincipal UserDetails user, @RequestParam(required = false) String uuid) {
        final var userZoneId = getUserTimeZoneId(userRepository.findById(user.getUsername()));

        if(uuid != null) {
            return calendlyService.getEventAvailability(user.getUsername(), uuid);
        }

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


        final var incorrectTo = request.stream().filter(e ->
            e.from().time().isAfter(e.to().time()) && e.from().day().equals(e.to().day())
        ).findAny();

        if (incorrectTo.isPresent()) {
            throw new ApiValidationException(FIELD_OUTSIDE_OF_FRAME, "to", incorrectTo.orElseThrow().to().toString() , "the field to cannot be before the field from in a single day event");
        }

        final var incorrectDay = request.stream().filter(e ->
            !e.from().day().equals(e.to().day()) && !e.from().day().equals(e.to().day().minus(1))
        ).findAny();


        if (incorrectDay.isPresent()) {
            throw new ApiValidationException(new ApiValidationException.Error(MORE_THEN_24_EVENT,
                List.of(
                    new ApiValidationException.ErrorField("from", incorrectDay.orElseThrow().from().toString()),
                    new ApiValidationException.ErrorField("to", incorrectDay.orElseThrow().to().toString())
                )),
                "events longer that one day are not supported"
            );
        }

        coachAvailabilityService.setRecurringAvailability(user.getUsername(), request);
    }


    @Secured("COACH")
    @GetMapping("/event-types")
    public List<EventType> getCalendlyEvents(@AuthenticationPrincipal UserDetails user) {
        return calendlyService.getEventTypes(user.getUsername());
    }


    @Secured("COACH")
    @PatchMapping("/recurring/settings")
    public void updateRecurringSetting(@AuthenticationPrincipal UserDetails user, @RequestBody ReoccurringAvailabilityRequest request) {
        availabilitySettingRepository.save(new CoachAvailabilitySettings().setCoach(user.getUsername()).
                setResource(request.uuid)
                .setType(request.type)
                .setActive(request.active)
        );
    }

    public record ReoccurringAvailabilityRequest(String uuid , CalendarSyncInfo.SyncType type, boolean active) {
    }

    public record EventFilterDto(@NotNull LocalDateTime from, @NotNull LocalDateTime to) {
    }

    public record SetNonrecurringRequestDto(
        @NotNull EventFilterDto timeFrame,
        @NotNull List<NonReoccurringEventDto> events
    ) {
    }
}
