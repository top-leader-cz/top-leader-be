/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.exception.ApiValidationException;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.notification.Notification;
import com.topleader.topleader.notification.NotificationService;
import com.topleader.topleader.notification.context.CoachLinkedNotificationContext;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.user.userinsight.UserInsightService;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.flywaydb.core.internal.util.CollectionsUtils;
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

import static com.topleader.topleader.exception.ErrorCodeConstants.SESSION_IN_PAST;
import static com.topleader.topleader.util.common.user.UserUtils.getUserTimeZoneId;
import static java.util.Objects.nonNull;
import static java.util.stream.Collectors.toMap;


/**
 * @author Daniel Slavik
 */
@RestController
@RequestMapping("/api/latest/user-info")
@AllArgsConstructor
public class UserInfoController {

    private final UserInfoService userInfoService;

    private final UserRepository userRepository;

    private final NotificationService notificationService;

    private final ScheduledSessionService scheduledSessionService;

    private final UserInsightService userInsightService;


    @GetMapping
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetails user) {
        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            userRepository.findById(user.getUsername()).orElseThrow()
        );
    }

    @PostMapping("/locale")
    public UserInfoDto setLocale(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetLocaleRequestDto request) {
        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            userRepository.findById(user.getUsername())
                .map(u -> u.setLocale(request.locale()))
                .map(userRepository::save)
                .orElseThrow()
        );
    }

    @PostMapping("/notes")
    public UserInfoDto setNotes(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetNotesRequestDto request) {
        return UserInfoDto.from(
            userInfoService.setNotes(user.getUsername(), request.notes()),
            userRepository.findById(user.getUsername()).orElseThrow()
        );
    }

    @PostMapping("/strengths")
    public UserInfoDto setStrengths(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        return UserInfoDto.from(
            userInfoService.setStrengths(user.getUsername(), request.data()),
            userRepository.findById(user.getUsername()).orElseThrow()
        );
    }


    @PostMapping("/values")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid ListDataRequestDto request) {
        var userInfo = userInfoService.setValues(user.getUsername(), request.data());
        if(shouldQueryAi(userInfo)) {
            userInsightService.setLeadershipStyleAnalysis(user.getUsername(),  userInfo.getStrengths(), userInfo.getValues());
        }
        return UserInfoDto.from(
            userInfo,
            userRepository.findById(user.getUsername()).orElseThrow()
        );
    }

    @PostMapping("/timezone")
    public UserInfoDto setValues(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetTimezoneRequestDto request) {

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            userRepository.findById(user.getUsername())
                .map(u -> u.setTimeZone(request.timezone()))
                .map(userRepository::save)
                .orElseThrow()
        );
    }

    @Transactional
    @PostMapping("/coach")
    public UserInfoDto setCoach(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid SetCoachRequestDto request) {

        final var currentUser = userRepository.findById(user.getUsername()).orElseThrow();

        if (!Objects.equals(request.coach(), currentUser.getCoach())) {
            currentUser.setCoach(request.coach());
            userRepository.save(currentUser);
            scheduledSessionService.deleteUserSessions(user.getUsername());
            if (nonNull(request.coach())) {
                notificationService.addNotification(new NotificationService.CreateNotificationRequest(
                    request.coach(),
                    Notification.Type.COACH_LINKED,
                    new CoachLinkedNotificationContext().setUsername(user.getUsername())
                ));
            }
        }

        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
            userRepository.findById(user.getUsername())
                .map(u -> u.setCoach(request.coach()))
                .map(userRepository::save)
                .orElseThrow()
        );
    }

    @GetMapping("/upcoming-sessions")
    public List<UpcomingSessionDto> getUpcomingSessions(@AuthenticationPrincipal UserDetails user) {

        final var sessions = scheduledSessionService.listUsersFutureSessions(user.getUsername());

        if (sessions.isEmpty()) {
            return List.of();
        }

        final var coaches = userRepository.findAllById(sessions.stream()
                .map(ScheduledSession::getCoachUsername)
                .collect(Collectors.toSet())
            ).stream()
            .collect(toMap(User::getUsername, Function.identity()));


        return sessions.stream()
            .map(s -> UpcomingSessionDto.from(s, Optional.ofNullable(coaches.get(s.getCoachUsername()))))
            .sorted(Comparator.comparing(UpcomingSessionDto::time))
            .toList();

    }

    @Transactional
    @PostMapping("/private-session")
    public UpcomingSessionDto schedulePrivateSession(@RequestBody SchedulePrivateSessionRequest request, @AuthenticationPrincipal UserDetails user) {

        final var time = request.time();

        final var userZoneId = getUserTimeZoneId(userRepository.findById(user.getUsername()));

        final var shiftedTime = time
            .atZone(userZoneId)
            .withZoneSameInstant(ZoneOffset.UTC)
            .toLocalDateTime();

        if (shiftedTime.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new ApiValidationException(SESSION_IN_PAST, "time", time.toString(), "Not possible to schedule session in past.");
        }

        return UpcomingSessionDto.from(
            scheduledSessionService.scheduleSession(new ScheduledSession()
                .setPaid(true)
                .setPrivate(true)
                .setTime(shiftedTime)
                .setUsername(user.getUsername())
            ),
            Optional.empty()
        );
    }

    @Transactional
    @DeleteMapping("/upcoming-sessions/{sessionId}")
    public void cancelSession(@PathVariable Long sessionId, @AuthenticationPrincipal UserDetails user) {

        if (scheduledSessionService.listUsersFutureSessions(user.getUsername())
            .stream().noneMatch(s -> s.getId().equals(sessionId))
        ) {
            throw new NotFoundException();
        }

        scheduledSessionService.cancelSession(sessionId);
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


    public record UserInfoDto(
        String username,
        String firstName,
        String lastName,
        Set<User.Authority> userRoles,
        String timeZone,
        List<String> strengths,
        List<String> values,
        List<String> areaOfDevelopment,
        String notes,
        String coach,
        String locale
    ) {
        public static UserInfoDto from(UserInfo info, User user) {
            return new UserInfoDto(
                info.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getAuthorities(),
                user.getTimeZone(),
                info.getStrengths(),
                info.getValues(),
                info.getAreaOfDevelopment(),
                info.getNotes(),
                user.getCoach(),
                user.getLocale()
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
