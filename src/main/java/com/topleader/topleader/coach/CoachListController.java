/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach;

import com.topleader.topleader.coach.availability.CoachAvailability;
import com.topleader.topleader.coach.availability.CoachAvailabilityService;
import com.topleader.topleader.coach.availability.DayType;
import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.util.image.ImageUtil;
import com.topleader.topleader.util.page.PageDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasExperienceFrom;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasExperienceTo;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasFieldsInList;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasLanguagesInList;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.hasRateInSet;
import static com.topleader.topleader.coach.CoachJpaSpecificationUtils.nameStartsWith;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/coaches")
public class CoachListController {

    private final CoachRepository coachRepository;

    private final CoachImageRepository coachImageRepository;

    private final CoachAvailabilityService coachAvailabilityService;

    private final ScheduledSessionService scheduledSessionService;

    @Transactional
    @PostMapping("/{username}/schedule")
    public void scheduleSession(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable String username,
        @RequestBody ScheduleSessionRequest request
    ) {

        final var requestedDay = request.firstDayOfTheWeek().plusDays(request.day().getDayOffset());

        if (requestedDay.isBefore(LocalDate.now())) {
            throw new ApiValidationException("firstDayOfTheWeek", "Not possible to schedule session in past.");
        }


        final var requestedDayTime = LocalDateTime.of(requestedDay, request.time());

        final var possibleStartDates = coachAvailabilityService.getWeekAvailabilitySplitIntoHours(username, request.firstDayOfTheWeek, request.day()).stream()
            .map(a -> LocalDateTime.of(a.getDate(), a.getTimeFrom()))
            .collect(Collectors.toSet());

        if (!possibleStartDates.contains(requestedDayTime)) {
            throw new ApiValidationException("time", "Time " + request.time() + " is not available");
        }

        if (scheduledSessionService.isAlreadyScheduled(username, requestedDayTime)) {
            throw new ApiValidationException("time", "Time " + request.time() + " is not available");
        }

        scheduledSessionService.scheduleSession(
            new ScheduledSession()
                .setUsername(user.getUsername())
                .setCoachUsername(username)
                .setTime(requestedDayTime)
                .setFirstDayOfTheWeek(request.firstDayOfTheWeek())
        );

    }

    @GetMapping("/{username}/availability")
    public Map<DayType, List<CoachAvailabilityDto>> getCoachAvailability(
        @PathVariable String username,
        @RequestParam LocalDate firstDayOfTheWeek
    ) {
        return coachAvailabilityService.getWeekAvailabilitySplitIntoHours(username, firstDayOfTheWeek).stream()
            .map(a -> Map.entry(a.getDay(), CoachAvailabilityDto.from(a)))
            .collect(groupingBy(Map.Entry::getKey, mapping(Map.Entry::getValue, toList())));
    }

    @GetMapping("/{username}/photo")
    public ResponseEntity<byte[]> getCoachPhoto(@PathVariable String username) {

        return coachImageRepository.findById(username)
            .map(i -> ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(i.getType()))
                .body(ImageUtil.decompressImage(i.getImageData()))
            )
            .orElseThrow(NotFoundException::new);
    }

    @GetMapping("/{username}")
    public CoachListDto findCoach(@PathVariable String username) {
        return coachRepository.findById(username)
            .map(CoachListDto::from)
            .orElseThrow(NotFoundException::new);
    }

    @PostMapping
    public Page<CoachListDto> findCoaches(@RequestBody @Valid FilterRequest request) {
        return findCoaches(request.toSpecification(), request.page().toPageable())
            .map(CoachListDto::from);
    }

    private Page<Coach> findCoaches(List<Specification<Coach>> filter, Pageable page) {
        return filter.isEmpty() ?
            coachRepository.findAll(page) :
            coachRepository.findAll(Specification.allOf(filter), page);

    }

    public record CoachAvailabilityDto(
        DayType day,
        LocalDate date,
        LocalTime timeFrom,
        LocalTime timeTo,

        LocalDate firstDayOfTheWeek
    ) {
        public static CoachAvailabilityDto from(CoachAvailability c) {
            return new CoachAvailabilityDto(
                c.getDay(),
                c.getDate(),
                c.getTimeFrom(),
                c.getTimeTo(),
                c.getFirstDayOfTheWeek()
            );
        }
    }

    public record ScheduleSessionRequest(LocalDate firstDayOfTheWeek, DayType day, LocalTime time) {
    }

    public record CoachListDto(
        String username,
        String firstName,
        String lastName,
        String email,

        String bio,

        Set<String> languages,

        Set<String> fields,

        Integer experience,

        String rate
    ) {
        public static CoachListDto from(Coach c) {
            return new CoachListDto(
                c.getUsername(),
                c.getFirstName(),
                c.getLastName(),
                c.getEmail(),
                c.getBio(),
                c.getLanguages(),
                c.getFields(),
                toExperience(c.getExperienceSince()),
                c.getRate()
            );
        }

        private static Integer toExperience(LocalDate experienceSince) {
            return Optional.ofNullable(experienceSince)
                .map(LocalDate::getYear)
                .map(year -> LocalDate.now().getYear() - year)
                .orElse(null);
        }
    }

    public record FilterRequest(
        @NotNull
        PageDto page,
        List<String> languages,
        List<String> fields,
        Integer experienceFrom,
        Integer experienceTo,
        List<String> prices,
        String name
    ) {

        public List<Specification<Coach>> toSpecification() {

            final var result = new ArrayList<Specification<Coach>>();

            Optional.ofNullable(languages())
                .ifPresent(languages -> result.add(hasLanguagesInList(languages)));

            Optional.ofNullable(fields())
                .ifPresent(fields -> result.add(hasFieldsInList(fields)));

            Optional.ofNullable(experienceFrom())
                .ifPresent(from -> result.add(hasExperienceFrom(toDate(experienceFrom()))));

            Optional.ofNullable(experienceTo())
                .ifPresent(to -> result.add(hasExperienceTo(toDate(experienceTo()))));

            Optional.ofNullable(prices())
                .ifPresent(prices -> result.add(hasRateInSet(prices)));

            Optional.ofNullable(name())
                .ifPresent(name -> result.add(nameStartsWith(name)));

            return result;
        }

        private static LocalDate toDate(Integer i) {
            return LocalDate.now()
                .withMonth(1)
                .withDayOfMonth(1)
                .minusYears(i);
        }

    }
}
