/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.common.email.EmailTemplateService;
import com.topleader.topleader.common.email.SessionEmailData;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.hr.company.Company;
import com.topleader.topleader.hr.company.CompanyRepository;
import com.topleader.topleader.common.notification.Notification;
import com.topleader.topleader.common.notification.NotificationService;
import com.topleader.topleader.common.notification.context.CoachLinkedNotificationContext;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionQueryRepository;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinsight.UserInsightService;
import org.springframework.transaction.annotation.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.SESSION_CANCEL_TOO_LATE;
import static com.topleader.topleader.common.exception.ErrorCodeConstants.SESSION_IN_PAST;
import static com.topleader.topleader.common.util.common.user.UserUtils.getUserTimeZoneId;
import static java.util.Objects.nonNull;
import static java.util.function.Predicate.not;
import static java.util.stream.Collectors.toMap;


/**
 * @author Daniel Slavik
 */
@RestController
@RequestMapping("/api/latest/user-info")
@RequiredArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final ScheduledSessionService scheduledSessionService;

    private final UserInsightService userInsightService;

    private final CompanyRepository companyRepository;

    private final EmailTemplateService emailTemplateService;


    @GetMapping
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetails user) {

        final var dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    @PostMapping("/locale")
    public UserInfoDto setLocale(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetLocaleRequestDto request) {

        final var dbUser = userRepository.findByUsername(user.getUsername())
            .map(u -> u.setLocale(request.locale()))
            .map(userRepository::save)
            .orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    @PostMapping("/notes")
    public SetNotesRequestDto setNotes(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetNotesRequestDto request) {

        return new SetNotesRequestDto(
            userInfoService.setNotes(user.getUsername(), request.notes())
                .getNotes()
        )
            ;
    }

    @GetMapping("/notes")
    public SetNotesRequestDto getNotes(@AuthenticationPrincipal UserDetails user) {
        return new SetNotesRequestDto(
            userInfoService.find(user.getUsername())
                .getNotes()
        );
    }

    @PostMapping("/strengths")
    public UserInfoDto setStrengths(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        var userInfo = userInfoService.setStrengths(user.getUsername(), request.data());
        if (shouldQueryAi(userInfo)) {
            userInsightService.setUserInsight(userInfo);
        }

        final var dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);


        return UserInfoDto.from(
            userInfo,
            dbUser,
            company
        );
    }


    @PostMapping("/values")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        var userInfo = userInfoService.setValues(user.getUsername(), request.data());
        if (shouldQueryAi(userInfo)) {
            userInsightService.setUserInsight(userInfo);
        }

        final var dbUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);

        return UserInfoDto.from(
            userInfo,
            dbUser,
            company
        );
    }

    @PostMapping("/timezone")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetTimezoneRequestDto request) {

        final var dbUser = userRepository.findByUsername(user.getUsername())
            .map(u -> u.setTimeZone(request.timezone()))
            .map(userRepository::save)
            .orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);


        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    @Transactional
    @PostMapping("/coach")
    public UserInfoDto setCoach(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetCoachRequestDto request) {

        final var currentUser = userRepository.findByUsername(user.getUsername()).orElseThrow(NotFoundException::new);

        if (!Objects.equals(request.coach(), currentUser.getCoach())) {
            currentUser.setCoach(request.coach());
            userRepository.save(currentUser);
            scheduledSessionService.deleteUserCoachedSessions(user.getUsername());
            if (nonNull(request.coach())) {
                emailTemplateService.sentPickedMessage(request.coach());
                notificationService.addNotification(new NotificationService.CreateNotificationRequest(
                    request.coach(),
                    Notification.Type.COACH_LINKED,
                    new CoachLinkedNotificationContext().setUsername(user.getUsername())
                ));
            }
        }

        final var dbUser = userRepository.findByUsername(user.getUsername())
            .map(u -> u.setCoach(request.coach()))
            .map(userRepository::save)
            .orElseThrow(NotFoundException::new);
        final var company = Optional.ofNullable(dbUser.getCompanyId()).flatMap(companyRepository::findById);


        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            dbUser,
            company
        );
    }

    @GetMapping("/upcoming-sessions")
    public List<SessionDto> getUpcomingSessions(@AuthenticationPrincipal UserDetails user) {
        return getSessionDtos(scheduledSessionService.listUsersFutureSessions(user.getUsername()));
    }

    @GetMapping("/sessions")
    public SessionsResponse getAllSessions(@AuthenticationPrincipal UserDetails user) {
        var sessions = getSessionDtos(scheduledSessionService.listUsersSessions(user.getUsername()));
        var summary = scheduledSessionService.getSessionSummary(user.getUsername());
        return new SessionsResponse(summary, sessions);
    }

    private List<SessionDto> getSessionDtos(List<ScheduledSession> sessions) {
        if (sessions.isEmpty()) {
            return List.of();
        }

        var coaches = userRepository.findAllByUsernameIn(sessions.stream()
                .map(ScheduledSession::getCoachUsername)
                .collect(Collectors.toSet())
            ).stream()
            .collect(toMap(User::getUsername, Function.identity()));

        return sessions.stream()
            .map(s -> SessionDto.from(s, Optional.ofNullable(coaches.get(s.getCoachUsername()))))
            .sorted(Comparator.comparing(SessionDto::time).reversed())
            .toList();
    }

    @PostMapping("/private-session")
    public UpcomingSessionDto schedulePrivateSession(@RequestBody SchedulePrivateSessionRequest request, @AuthenticationPrincipal UserDetails user) {
        final var time = request.time();

        final var userZoneId = getUserTimeZoneId(userRepository.findByUsername(user.getUsername()));

        final var shiftedTime = time
            .atZone(userZoneId)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();

        if (shiftedTime.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new ApiValidationException(SESSION_IN_PAST, "time", time.toString(), "Not possible to schedule session in past.");
        }

        final var session = scheduledSessionService.scheduleSession(new ScheduledSession()
            .setPaid(true)
            .setPrivate(true)
            .setTime(shiftedTime)
            .setUsername(user.getUsername()),
            user.getUsername()
        );

        emailTemplateService.sendBookingAlertPrivateSessionEmail(
            new SessionEmailData(
                session.getId(),
                session.getUsername(),
                session.getCoachUsername(),
                session.getTime()
            )
        );

        return UpcomingSessionDto.from(
            session,
            Optional.empty()
        );
    }


    @DeleteMapping("/upcoming-sessions/{sessionId}")
    public void cancelSession(@PathVariable Long sessionId, @AuthenticationPrincipal UserDetails user) {

        final var session = scheduledSessionService.getFutureSession(sessionId)
            .filter(s -> s.getUsername().equals(user.getUsername()))
            .orElseThrow(NotFoundException::new);

        if (session.getTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new ApiValidationException(SESSION_CANCEL_TOO_LATE, "sessionId", sessionId.toString(),
                "Cannot cancel session less than 24 hours before start time");
        }

        final var sessionData = new SessionEmailData(
            session.getId(),
            session.getUsername(),
            session.getCoachUsername(),
            session.getTime()
        );
        if (session.isPrivate()) {
            emailTemplateService.sendCancelAlertPrivateSessionEmail(sessionData);
        } else {
            emailTemplateService.sendCancelAlertEmail(sessionData);
        }
        scheduledSessionService.cancelSession(sessionId, user.getUsername());
    }

    public record SchedulePrivateSessionRequest(LocalDateTime time) {
    }

    public record SetNotesRequestDto(String notes) {

    }

    public record SetLocaleRequestDto(@Pattern(regexp = "[a-z]{2}") String locale) {

    }

    public record UpcomingSessionDto(
        Long id,
        String coach,
        String firstName,
        String lastName,
        LocalDateTime time,
        boolean isPrivate
    ) {
        public static UpcomingSessionDto from(ScheduledSession s, Optional<User> u) {
            return new UpcomingSessionDto(
                s.getId(),
                u.map(User::getUsername).orElse(null),
                u.map(User::getFirstName).orElse(null),
                u.map(User::getLastName).orElse(null),
                s.getTime(),
                s.isPrivate()
            );
        }
    }

    public record SessionsResponse(
        ScheduledSessionQueryRepository.SessionSummary summary,
        List<SessionDto> sessions
    ) {}

    public record SessionDto(
        Long id,
        String coach,
        String firstName,
        String lastName,
        LocalDateTime time,
        boolean isPrivate,
        ScheduledSession.Status status
    ) {
        public static SessionDto from(ScheduledSession s, Optional<User> u) {
            return new SessionDto(
                s.getId(),
                u.map(User::getUsername).orElse(null),
                u.map(User::getFirstName).orElse(null),
                u.map(User::getLastName).orElse(null),
                s.getTime(),
                s.isPrivate(),
                s.getStatus()
            );
        }
    }


    public record UserInfoDto(
        String username,
        String firstName,
        String lastName,
        Set<User.Authority> userRoles,
        String timeZone,
        List<String> strengths,
        List<String> values,
        List<String> areaOfDevelopment,
        String coach,
        String locale,
        Set<String> allowedCoachRates,
        Long companyId
    ) {
        public static UserInfoDto from(UserInfo info, User user, Optional<Company> company) {
            return new UserInfoDto(
                info.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getAuthorities(),
                user.getTimeZone(),
                info.getStrengths(),
                info.getValues(),
                info.getAreaOfDevelopment(),
                user.getCoach(),
                user.getLocale(),
                Optional.ofNullable(user.getAllowedCoachRates())
                    .filter(not(CollectionUtils::isEmpty))
                    .orElse(company.map(Company::getAllowedCoachRates).orElse(null)),
                user.getCompanyId()
            );
        }
    }

    public record ListDataRequestDto(@NotEmpty List<String> data) {
    }

    public record SetTimezoneRequestDto(@Size(min = 1, max = 20) String timezone) {
    }

    public record SetCoachRequestDto(String coach) {
    }

    private boolean shouldQueryAi(UserInfo userInfo) {
        return !CollectionUtils.isEmpty(userInfo.getValues()) && !CollectionUtils.isEmpty(userInfo.getStrengths());
    }
}
