/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachImageRepository;
import com.topleader.topleader.coach.availability.CoachAvailabilityService;
import com.topleader.topleader.common.email.SessionEmailData;
import com.topleader.topleader.hr.company.Company;
import com.topleader.topleader.hr.company.CompanyRepository;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionService;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

    private final CoachListViewRepository coachListViewRepository;

    private final CoachAvailabilityService coachAvailabilityService;

    private final ScheduledSessionService scheduledSessionService;

    private final UserRepository userRepository;

    private final EmailTemplateService emailTemplateService;

    private final CompanyRepository companyRepository;

    @PostMapping("/{username}/schedule")
    public void scheduleSession(
        @AuthenticationPrincipal UserDetails user,
        @PathVariable String username,
        @RequestBody ScheduleSessionRequest request
    ) {
        final var userInDb = userRepository.findByUsername(user.getUsername()).orElseThrow();

        if (hasText(userInDb.getCoach()) && !userInDb.getCoach().equals(username)) {
            throw new ApiValidationException(DIFFERENT_COACH_NOT_PERMITTED, "username", username, "Cannot schedule a session with a different coach then already picked");
        }

        if (isNull(userInDb.getCoach())) {
            userRepository.save(userInDb.setCoach(username));
        }

        scheduleSession(user.getUsername(), username, request.time(), true);
    }

    @PostMapping("/schedule")
    public void scheduleSession(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody ScheduleSessionRequest request
    ) {

        final var coachName = userRepository.findByUsername(user.getUsername()).orElseThrow().getCoach();

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

        final var user = userRepository.findByUsername(clientName).orElseThrow();

        final var session = scheduledSessionService.scheduleSession(
            new ScheduledSession()
                .setUsername(clientName)
                .setCoachUsername(coachName)
                .setTime(shiftedTime)
                .setPaid(coachName.equalsIgnoreCase(user.getFreeCoach()))
                .setPrivate(false),
            clientName
        );

        emailTemplateService.sendBookingAlertEmail(
            new SessionEmailData(
                session.getId(),
                session.getUsername(),
                session.getCoachUsername(),
                session.getTime()
            )
        );
    }

    @GetMapping("/{username}/availability")
    public List<ZonedDateTime> getCoachAvailability(
        @PathVariable String username,
        @RequestParam LocalDateTime from,
        @RequestParam LocalDateTime to,
        @AuthenticationPrincipal UserDetails loggedUser
        ) {

        final var userZoneId = getUserTimeZoneId(userRepository.findByUsername(loggedUser.getUsername()));

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

        return coachImageRepository.findByUsername(username)
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
        final var dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow();
        final var allowedRates = getAllowedRates(dbUser);

        var allCoaches = coachListViewRepository.findAllPublic();

        var filtered = allCoaches.stream()
                .filter(c -> allowedRates == null || allowedRates.isEmpty() || allowedRates.contains(c.getRate()))
                .filter(request.toFilter())
                .sorted(Comparator.comparing(CoachListView::getPriority).reversed()
                        .thenComparing(CoachListView::getUsername))
                .toList();

        var pageable = request.page().toPageable();
        int start = (int) Math.min(pageable.getOffset(), filtered.size());
        int end = (int) Math.min(start + pageable.getPageSize(), filtered.size());

        var content = filtered.subList(start, end).stream()
                .map(CoachListDto::from)
                .toList();

        return new PageImpl<>(content, pageable, filtered.size());
    }

    private Set<String> getAllowedRates(User user) {
        var userRates = userRepository.findAllowedCoachRates(user.getUsername());
        return Optional.ofNullable(userRates)
            .filter(not(List::isEmpty))
            .map(Set::copyOf)
            .or(() -> Optional.ofNullable(user.getCompanyId())
                .flatMap(companyRepository::findById)
                .map(Company::getAllowedCoachRates)
            )
            .filter(not(Set::isEmpty))
            .orElse(null);
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
                    c.getLanguagesSet(),
                    c.getFieldsSet(),
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

        public Predicate<CoachListView> toFilter() {
            Predicate<CoachListView> filter = c -> true;

            if (languages != null && !languages.isEmpty()) {
                filter = filter.and(c -> {
                    var coachLangs = c.getLanguagesSet();
                    return coachLangs != null && languages.stream().anyMatch(coachLangs::contains);
                });
            }

            if (fields != null && !fields.isEmpty()) {
                filter = filter.and(c -> {
                    var coachFields = c.getFieldsSet();
                    return coachFields != null && fields.stream().anyMatch(coachFields::contains);
                });
            }

            if (experienceFrom != null) {
                var fromDate = toDate(experienceFrom);
                filter = filter.and(c -> c.getExperienceSince() != null && !c.getExperienceSince().isAfter(fromDate));
            }

            if (experienceTo != null) {
                var toDate = toDate(experienceTo);
                filter = filter.and(c -> c.getExperienceSince() != null && !c.getExperienceSince().isBefore(toDate));
            }

            if (prices != null && !prices.isEmpty()) {
                filter = filter.and(c -> prices.contains(c.getRate()));
            }

            if (hasText(name)) {
                var nameLower = name.toLowerCase();
                filter = filter.and(c ->
                        (c.getFirstName() != null && c.getFirstName().toLowerCase().startsWith(nameLower)) ||
                        (c.getLastName() != null && c.getLastName().toLowerCase().startsWith(nameLower))
                );
            }

            return filter;
        }

        private static LocalDate toDate(Integer i) {
            return LocalDate.now()
                .withMonth(1)
                .withDayOfMonth(1)
                .minusYears(i);
        }

    }
}
