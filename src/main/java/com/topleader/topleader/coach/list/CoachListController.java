/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachImageRepository;
import com.topleader.topleader.coach.availability.CoachAvailabilityService;
import com.topleader.topleader.hr.company.Company;
import com.topleader.topleader.hr.company.CompanyRepository;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionService;
import io.micrometer.core.instrument.Counter;
import com.topleader.topleader.common.email.EmailTemplateService;

import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.common.util.image.ImageUtil;
import com.topleader.topleader.common.util.page.PageDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.*;
import static com.topleader.topleader.common.util.common.user.UserUtils.getUserTimeZoneId;
import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static org.springframework.util.StringUtils.hasText;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/latest/coaches")
public class CoachListController {

    private final CoachImageRepository coachImageRepository;

    private final CoachListViewJooqRepository coachListViewRepository;

    private final CoachAvailabilityService coachAvailabilityService;

    private final ScheduledSessionService scheduledSessionService;

    private final UserRepository userRepository;

    private final EmailTemplateService emailTemplateService;

    private final CompanyRepository companyRepository;

    private final Counter sessionScheduledCounter;


    @Transactional
    @PostMapping("/{username}/schedule")
    public void scheduleSession(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable String username,
        @RequestBody ScheduleSessionRequest request
    ) {
        final var userInDb = userRepository.findById(user.getUsername()).orElseThrow();

        if (hasText(userInDb.getCoach()) && !userInDb.getCoach().equals(username)) {
            throw new ApiValidationException(DIFFERENT_COACH_NOT_PERMITTED, "username", username, "Cannot schedule a session with a different coach then already picked");
        }

        if (isNull(userInDb.getCoach())) {
            userRepository.save(userInDb.setCoach(username));
        }

        scheduleSession(user.getUsername(), username, request.time(), true);
    }

    @Transactional
    @PostMapping("/schedule")
    public void scheduleSession(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody ScheduleSessionRequest request
    ) {

        final var coachName = userRepository.findById(user.getUsername()).orElseThrow().getCoach();

        scheduleSession(user.getUsername(), coachName, request.time(), true);
    }

    private void scheduleSession(String clientName, String coachName, ZonedDateTime time, Boolean useFreeBusy) {

        final var shiftedTime = time
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();

        if (shiftedTime.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new ApiValidationException(SESSION_IN_PAST, "time", time.toString(), "Not possible to schedule session in past.");
        }

        final var possibleStartDates = coachAvailabilityService.getAvailabilitySplitIntoHoursFiltered(coachName, shiftedTime.plusDays(7), useFreeBusy);

        if (!possibleStartDates.contains(shiftedTime)) {
            throw new ApiValidationException(TIME_NOT_AVAILABLE, "time", time.toString(), "Time " + time + " is not available");
        }

        if (scheduledSessionService.isAlreadyScheduled(coachName, shiftedTime)) {
            throw new ApiValidationException(TIME_NOT_AVAILABLE, "time", time.toString(), "Time " + time + " is not available");
        }
        if (!scheduledSessionService.isPossibleToSchedule(clientName, coachName)) {
            throw new ApiValidationException(NOT_ENOUGH_CREDITS, "user", clientName, "User does not have enough credit");
        }

        final var user = userRepository.findById(clientName).orElseThrow();

        final var session = scheduledSessionService.scheduleSession(
            new ScheduledSession()
                .setUsername(clientName)
                .setCoachUsername(coachName)
                .setTime(shiftedTime)
                .setPaid(coachName.equalsIgnoreCase(user.getFreeCoach()))
                .setPrivate(false),
            clientName
        );

        sessionScheduledCounter.increment();
        emailTemplateService.sendBookingAlertEmail(session.getId());
    }

    @GetMapping("/{username}/availability")
    public List<ZonedDateTime> getCoachAvailability(
        @PathVariable String username,
        @RequestParam LocalDateTime from,
        @RequestParam LocalDateTime to,
        @AuthenticationPrincipal UserDetails loggedUser
        ) {

        final var userZoneId = getUserTimeZoneId(userRepository.findById(loggedUser.getUsername()));

        final var scheduledEvents = scheduledSessionService.listCoachesFutureSessions(username).stream()
                .map(ScheduledSession::getTime)
                .collect(Collectors.toSet());

        return coachAvailabilityService.getAvailabilitySplitIntoHoursFiltered(username, to, true).stream()
                .filter(not(scheduledEvents::contains))
                .map(d -> d.atZone(ZoneOffset.UTC).withZoneSameInstant(userZoneId))
                .toList();
    }

    @GetMapping("/{username}/photo")
    public ResponseEntity<byte[]> getCoachPhoto(@PathVariable String username) {

        return coachImageRepository.findById(username)
            .map(i -> ResponseEntity.status(HttpStatus.OK)
                .contentType(MediaType.valueOf(i.getType()))
                .body(ImageUtil.decompressImage(i.getImageData()))
            )
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{username}")
    public ResponseEntity<CoachListDto> findCoach(@PathVariable String username) {
        return coachListViewRepository.findById(username)
            .map(CoachListDto::from)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Page<CoachListDto> findCoaches(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody @Valid FilterRequest request
    ) {

        final var dbUser = userRepository.findById(user.getUsername()).orElseThrow();

        Set<String> allowedRates = getAllowedRates(dbUser, request.prices());

        var pageable = PageRequest.of(request.page().pageNumber(), request.page().pageSize());

        return coachListViewRepository.findAllWithFilters(request, allowedRates, pageable)
                .map(CoachListDto::from);
    }

    private Set<String> getAllowedRates(User user, List<String> requestedPrices) {
        Set<String> userRates = Optional.ofNullable(user.getAllowedCoachRates())
                .filter(not(Set::isEmpty))
                .or(() -> Optional.ofNullable(user.getCompanyId())
                        .flatMap(companyRepository::findById)
                        .map(Company::getAllowedCoachRateNames)
                )
                .orElse(null);

        if (userRates == null && requestedPrices == null) {
            return null;
        }
        if (userRates == null) {
            return new HashSet<>(requestedPrices);
        }
        if (requestedPrices == null) {
            return userRates;
        }
        Set<String> result = new HashSet<>(requestedPrices);
        result.retainAll(userRates);
        return result;
    }

    public record ScheduleSessionRequest(ZonedDateTime time) {
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

        String rate,

        Set<String> certificate,

        String timeZone,

        String webLink,

        String linkedinProfile,

        Set<Coach.PrimaryRole> primaryRoles


    ) {
        public static CoachListDto from(CoachListView c) {
            return new CoachListDto(
                    c.getUsername(),
                    c.getFirstName(),
                    c.getLastName(),
                    c.getEmail(),
                    c.getBio(),
                    c.getLanguages(),
                    c.getFields(),
                    toExperience(c.getExperienceSince()),
                    c.getRate(),
                    c.getCertificateSet(),
                    c.getTimeZone(),
                    c.getWebLink(),
                    c.getLinkedinProfile(),
                    c.getPrimaryRolesSet()
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
    }
}
