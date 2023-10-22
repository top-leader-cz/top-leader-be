/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.notification.Notification;
import com.topleader.topleader.notification.NotificationService;
import com.topleader.topleader.notification.context.CoachLinkedNotificationContext;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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


    @GetMapping
    public UserInfoDto getUserInfo(@AuthenticationPrincipal UserDetails user) {
        return UserInfoDto.from(
            userInfoService.find(user.getUsername()),
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
        return UserInfoDto.from(
            userInfoService.setValues(user.getUsername(), request.data()),
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
    public List<UpcomingSessionDto> setCoach(@AuthenticationPrincipal UserDetails user) {

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
            .map(s -> UpcomingSessionDto.from(s, coaches.get(s.getCoachUsername())))
            .toList();

    }

    public record UpcomingSessionDto(
        String coach,
        String firstName,
        String lastName,
        LocalDateTime time
    ) {

        public static UpcomingSessionDto from(ScheduledSession s, User u) {
            return new UpcomingSessionDto(
                u.getUsername(),
                u.getFirstName(),
                u.getLastName(),
                s.getTime()
            );
        }
    }


    public record UserInfoDto(
        String username,
        Set<User.Authority> userRoles,
        String timeZone,
        List<String> strengths,
        List<String> values,
        List<String> areaOfDevelopment,
        String notes,
        String coach
    ) {
        public static UserInfoDto from(UserInfo info, User user) {
            return new UserInfoDto(
                info.getUsername(),
                user.getAuthorities(),
                user.getTimeZone(),
                info.getStrengths(),
                info.getValues(),
                info.getAreaOfDevelopment(),
                info.getNotes(),
                user.getCoach()
            );
        }
    }

    public record ListDataRequestDto(@NotEmpty List<String> data) {
    }

    public record SetTimezoneRequestDto(@Size(min = 1, max = 20) String timezone) {
    }

    public record SetCoachRequestDto(String coach) {
    }
}
