package com.topleader.topleader.session.scheduled_session;

import com.topleader.topleader.common.email.EmailTemplateService;
import com.topleader.topleader.common.email.SessionEmailData;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.common.util.LocaleUtils;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.SESSION_CANCEL_TOO_LATE;
import static com.topleader.topleader.common.exception.ErrorCodeConstants.SESSION_IN_PAST;
import static java.util.stream.Collectors.toMap;

@RestController
@RequestMapping("/api/latest/user-info")
@RequiredArgsConstructor
public class UserInfoSessionController {

    private final ScheduledSessionService scheduledSessionService;
    private final ScheduledSessionQueryRepository scheduledSessionQueryRepository;
    private final UserRepository userRepository;
    private final EmailTemplateService emailTemplateService;

    @GetMapping("/upcoming-sessions")
    public List<SessionDto> getUpcomingSessions(@AuthenticationPrincipal UserDetails user) {
        return toSessionDtos(scheduledSessionService.listUsersFutureSessions(user.getUsername()));
    }

    @GetMapping("/sessions")
    public SessionsResponse getAllSessions(@AuthenticationPrincipal UserDetails user) {
        var sessions = toSessionDtos(scheduledSessionService.listUsersSessions(user.getUsername()));
        var summary = scheduledSessionService.getSessionSummary(user.getUsername());
        return new SessionsResponse(summary, sessions);
    }

    @PostMapping("/private-session")
    public UpcomingSessionDto schedulePrivateSession(@RequestBody SchedulePrivateSessionRequest request, @AuthenticationPrincipal UserDetails user) {
        var userZoneId = LocaleUtils.zoneIdOrUtc(userRepository.findByUsername(user.getUsername()).map(User::getTimeZone).orElse(null));
        var shiftedTime = request.time()
                .atZone(userZoneId)
                .withZoneSameInstant(ZoneOffset.UTC)
                .toLocalDateTime();

        if (shiftedTime.isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
            throw new ApiValidationException(SESSION_IN_PAST, "time", request.time().toString(), "Not possible to schedule session in past.");
        }

        var session = scheduledSessionService.scheduleSession(new ScheduledSession()
                .setPaid(true)
                .setPrivate(true)
                .setTime(shiftedTime)
                .setUsername(user.getUsername()),
                user.getUsername()
        );

        emailTemplateService.sendBookingAlertPrivateSessionEmail(
                new SessionEmailData(session.getId(), session.getUsername(), session.getCoachUsername(), session.getTime())
        );

        return UpcomingSessionDto.from(session, Optional.empty());
    }

    @DeleteMapping("/upcoming-sessions/{sessionId}")
    public void cancelSession(@PathVariable Long sessionId, @AuthenticationPrincipal UserDetails user) {
        var session = scheduledSessionService.getFutureSession(sessionId)
                .filter(s -> s.getUsername().equals(user.getUsername()))
                .orElseThrow(NotFoundException::new);

        if (session.getTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new ApiValidationException(SESSION_CANCEL_TOO_LATE, "sessionId", sessionId.toString(),
                    "Cannot cancel session less than 24 hours before start time");
        }

        var sessionData = new SessionEmailData(session.getId(), session.getUsername(), session.getCoachUsername(), session.getTime());
        if (session.isPrivate()) {
            emailTemplateService.sendCancelAlertPrivateSessionEmail(sessionData);
        } else {
            emailTemplateService.sendCancelAlertEmail(sessionData);
        }
        scheduledSessionService.cancelSession(sessionId, user.getUsername());
    }

    private List<SessionDto> toSessionDtos(List<ScheduledSession> sessions) {
        if (sessions.isEmpty()) {
            return List.of();
        }
        var coaches = userRepository.findAllByUsernameIn(
                sessions.stream().map(ScheduledSession::getCoachUsername).collect(Collectors.toSet())
        ).stream().collect(toMap(User::getUsername, Function.identity()));

        return sessions.stream()
                .map(s -> SessionDto.from(s, Optional.ofNullable(coaches.get(s.getCoachUsername()))))
                .sorted(Comparator.comparing(SessionDto::time).reversed())
                .toList();
    }

    public record SchedulePrivateSessionRequest(LocalDateTime time) {}

    public record UpcomingSessionDto(Long id, String coach, String firstName, String lastName, LocalDateTime time, boolean isPrivate) {
        public static UpcomingSessionDto from(ScheduledSession s, Optional<User> u) {
            return new UpcomingSessionDto(s.getId(), u.map(User::getUsername).orElse(null),
                    u.map(User::getFirstName).orElse(null), u.map(User::getLastName).orElse(null),
                    s.getTime(), s.isPrivate());
        }
    }

    public record SessionsResponse(ScheduledSessionQueryRepository.SessionSummary summary, List<SessionDto> sessions) {}

    public record SessionDto(Long id, String coach, String firstName, String lastName, LocalDateTime time, boolean isPrivate, ScheduledSession.Status status) {
        public static SessionDto from(ScheduledSession s, Optional<User> u) {
            return new SessionDto(s.getId(), u.map(User::getUsername).orElse(null),
                    u.map(User::getFirstName).orElse(null), u.map(User::getLastName).orElse(null),
                    s.getTime(), s.isPrivate(), s.getStatus());
        }
    }
}
