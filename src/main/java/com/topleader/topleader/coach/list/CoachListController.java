/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.coach.list;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachImageRepository;
import com.topleader.topleader.coach.availability.CoachAvailabilityService;
import com.topleader.topleader.email.EmailTemplateService;
import com.topleader.topleader.company.Company;
import com.topleader.topleader.company.CompanyRepository;
import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.util.image.ImageUtil;
import com.topleader.topleader.util.page.PageDto;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import static com.topleader.topleader.exception.ErrorCodeConstants.DIFFERENT_COACH_NOT_PERMITTED;
import static com.topleader.topleader.exception.ErrorCodeConstants.NOT_ENOUGH_CREDITS;
import static com.topleader.topleader.exception.ErrorCodeConstants.SESSION_IN_PAST;
import static com.topleader.topleader.exception.ErrorCodeConstants.TIME_NOT_AVAILABLE;
import static com.topleader.topleader.util.common.user.UserUtils.getUserTimeZoneId;
import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;
import static org.springframework.data.jpa.domain.Specification.allOf;
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

        final var possibleStartDates = coachAvailabilityService.getAvailabilitySplitIntoHoursFiltered(coachName, shiftedTime.minusDays(1), shiftedTime.plusDays(1), useFreeBusy);

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
                .setPrivate(false)
        );

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

        return coachAvailabilityService.getAvailabilitySplitIntoHoursFiltered(username, from, to, true).stream()
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
            .orElseThrow(NotFoundException::new);
    }

    @GetMapping("/{username}")
    public CoachListDto findCoach(@PathVariable String username) {
        return coachListViewRepository.findById(username)
            .map(CoachListDto::from)
            .orElseThrow(NotFoundException::new);
    }

    @PostMapping
    public Page<CoachListDto> findCoaches(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody @Valid FilterRequest request
    ) {

        final var dbUser = userRepository.findById(user.getUsername()).orElseThrow();

        return findCoaches(
            Stream.concat(maxRateFilter(dbUser).stream(), request.toSpecification().stream()).toList(),
            request.page().toPageable()
        )
            .map(CoachListDto::from);
    }

    private Page<CoachListView> findCoaches(List<Specification<CoachListView>> filter, Pageable page) {

        return coachListViewRepository.findAll(
            Optional.ofNullable(filter)
                .filter(not(List::isEmpty))
                .map(f -> allOf(f).and(isProfilePublic()))
                .orElse(isProfilePublic()),
            page
        );

    }

    private Optional<Specification<CoachListView>> maxRateFilter(User user) {
        return Optional.ofNullable(user.getAllowedCoachRates())
            .filter(not(Set::isEmpty))
            .or(() -> Optional.ofNullable(user.getCompanyId())
                .flatMap(companyRepository::findById)
                .map(Company::getAllowedCoachRates)
            )
            .filter(not(Set::isEmpty))
            .map(CoachListController::rateIn);
    }

    public static Specification<CoachListView> isProfilePublic() {
        return (root, query, criteriaBuilder) ->
            criteriaBuilder.isTrue(root.get("publicProfile"));
    }

    private static Specification<CoachListView> rateIn(Set<String> allowed) {
        return (root, query, criteriaBuilder) ->
            root.get("rate").in(allowed);
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

        Coach.CertificateType certificate,

        String timeZone,

        String webLink,

        String linkedinProfile,

        boolean freeSlots


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
                    c.getCertificate(),
                    c.getTimeZone(),
                    c.getWebLink(),
                    c.getLinkedinProfile(),
                    c.isFreeSlots()
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

        public List<Specification<CoachListView>> toSpecification() {

            final var result = new ArrayList<Specification<CoachListView>>();

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
