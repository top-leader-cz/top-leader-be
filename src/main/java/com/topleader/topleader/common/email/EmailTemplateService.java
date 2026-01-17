/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.email;

import com.topleader.topleader.common.calendar.ical.ICalService;
import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionService;
import com.topleader.topleader.user.UserRepository;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.USER_NOT_FOUND;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EmailTemplateService {

    private static final String PRIVATE_SESSION_EVENT_NAME = "Private session - TopLeader";

    private static final Map<String, String> subjects = Map.of(
        "en", "New Booking Alert on TopLeader",
        "cs", "Upozornění na novou rezervaci na TopLeader",
        "fr", "Alerte de nouvelle réservation sur TopLeader",
        "de", "Neue Buchungsalarm auf TopLeader");

    private static final Map<String, String> subjectUserInvitations = Map.of(
        "en", "Your Session Confirmation on TopLeader",
        "cs", "Potvrzení vašeho sezení v TopLeader platformě",
        "fr", "Bestätigung Ihres Coaching-Termins bei TopLeader",
        "de", "Confirmation de votre séance de coaching sur TopLeader");

    private static final Map<String, String> pickedCoachSubjects = Map.of(
            "en", "You’ve Been Selected as a Coach on TopLeader!",
            "cs", "Byli jste vybráni jako kouč na platformě TopLeader!",
            "fr", "Vous avez été sélectionné comme coach sur TopLeader!",
            "de", " Sie wurden als Coach auf TopLeader ausgewählt!");

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    private final UserRepository userRepository;

    private final EmailService emailService;

    private final TemplateService velocityService;

    private final ICalService iCalService;

    private final ScheduledSessionService sessionService;



    public void sendBookingAlertPrivateSessionEmail(Long sessionId) {
        final var session = sessionService.getSessionData(sessionId)
            .orElseThrow(NotFoundException::new);

        log.info("Sending reservation alert for: [{}]", session);

        final var user = userRepository.findByUsername(session.getUsername())
            .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", session.getTime().toString(), "User not found " + session.getUsername()));

        final var eventId = "session-" + sessionId;

        emailService.sendEmail(
            user.getEmail(),
            subjectUserInvitations.getOrDefault(user.getLocale(),
                defaultLocale),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "time", session.getTime().toString(),
                        "link", appUrl)
                ),
                parseUserTemplateName(user.getLocale())
            ),
            iCalService.createCalendarPrivateEvent(
                session.getTime(),
                session.getTime().plusHours(1),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                PRIVATE_SESSION_EVENT_NAME,
                eventId
            )
        );
    }

    public void sendBookingAlertEmail(Long sessionId) {
        final var session = sessionService.getSessionData(sessionId)
            .orElseThrow(NotFoundException::new);

        log.info("Sending reservation alert for: [{}]", session);

        final var user = userRepository.findByUsername(session.getUsername())
            .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", session.getTime().toString(), "User not found " + session.getUsername()));
        final var coach = userRepository.findByUsername(session.getCoachUsername())
            .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", session.getTime().toString(), "Coach not found " + session.getCoachUsername()));

        final var eventId = "session-" + sessionId;

        emailService.sendEmail(
                coach.getEmail(),
            subjects.getOrDefault(coach.getLocale(),
                defaultLocale),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", coach.getFirstName(),
                        "lastName", coach.getLastName(),
                        "link", appUrl)
                ),
                parseCoachTemplateName(coach.getLocale())),
            iCalService.createCalendarEvent(
                session.getTime(),
                session.getTime().plusHours(1),
                coach.getEmail(),
                coach.getFirstName() + " " + coach.getLastName(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                "Session with " + user.getFirstName() + " " + user.getLastName(),
                eventId
            )
        );

        emailService.sendEmail(
             user.getEmail(),
            subjectUserInvitations.getOrDefault(user.getLocale(),
                defaultLocale),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "time", session.getTime().toString(),
                        "link", appUrl)
                ),
                parseUserTemplateName(user.getLocale())
            ),
            iCalService.createCalendarEvent(
                session.getTime(),
                session.getTime().plusHours(1),
                coach.getEmail(),
                coach.getFirstName() + " " + coach.getLastName(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                "Session with " + coach.getFirstName() + " " + coach.getLastName(),
                eventId
            )
        );
    }


    public void sendCancelAlertEmail(Long sessionId) {

        final var session = sessionService.getSessionData(sessionId)
            .orElseThrow(NotFoundException::new);

        log.info("Sending cancel alert for: [{}]", session);

        final var user = userRepository.findByUsername(session.getUsername())
            .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", session.getTime().toString(), "User not found " + session.getUsername()));
        final var coach = userRepository.findByUsername(session.getCoachUsername())
            .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", session.getTime().toString(), "Coach not found " + session.getCoachUsername()));

        final var eventId = "session-" + sessionId;

        emailService.sendEmail(
            coach.getEmail(),
            subjects.getOrDefault(coach.getLocale(),
                defaultLocale),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", coach.getFirstName(),
                        "lastName", coach.getLastName(),
                        "link", appUrl)
                ),
                "templates/reservation/coach-cancel.html"),
            iCalService.cancelCalendarEvent(
                session.getTime(),
                session.getTime().plusHours(1),
                coach.getEmail(),
                coach.getFirstName() + " " + coach.getLastName(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                "Session with " + user.getFirstName() + " " + user.getLastName(),
                eventId
            )
        );

        emailService.sendEmail(
            user.getEmail(),
            subjects.getOrDefault(user.getLocale(),
                defaultLocale),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "time", session.getTime().toString(),
                        "link", appUrl)
                ),
                "templates/reservation/user-cancel.html"
            ),
            iCalService.cancelCalendarEvent(
                session.getTime(),
                session.getTime().plusHours(1),
                coach.getEmail(),
                coach.getFirstName() + " " + coach.getLastName(),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                "Session with " + coach.getFirstName() + " " + coach.getLastName(),
                eventId
            )
        );
    }

    public void sendCancelAlertPrivateSessionEmail(Long sessionId) {

        final var session = sessionService.getSessionData(sessionId)
            .orElseThrow(NotFoundException::new);

        log.info("Sending cancel alert for: [{}]", session);

        final var user = userRepository.findByUsername(session.getUsername())
            .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", session.getTime().toString(), "User not found " + session.getUsername()));

        final var eventId = "session-" + sessionId;

        emailService.sendEmail(
            user.getEmail(),
            subjects.getOrDefault(user.getLocale(),
                defaultLocale),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName(),
                        "time", session.getTime().toString(),
                        "link", appUrl)
                ),
                "templates/reservation/user-cancel.html"
            ),
            iCalService.cancelCalendarPrivateEvent(
                session.getTime(),
                session.getTime().plusHours(1),
                user.getEmail(),
                user.getFirstName() + " " + user.getLastName(),
                PRIVATE_SESSION_EVENT_NAME,
                eventId
            )
        );
    }

    public void sentPickedMessage(String coachUsername) {
        log.info("Sending coach picked email for coach: [{}]", coachUsername);

        final var user = userRepository.findByUsername(coachUsername)
                .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", coachUsername, "User not found " + coachUsername ));

        emailService.sendEmail(
                user.getEmail(),
                pickedCoachSubjects.getOrDefault(user.getLocale(), defaultLocale),
                velocityService.getMessage(
                        new HashMap<>(
                                Map.of(
                                        "firstName", user.getFirstName(),
                                        "lastName", user.getLastName(),
                                        "link", appUrl)
                        ),
                        parsePickedCoachTemplateName(user.getLocale()))
               );
    }

    private String parseCoachTemplateName(String locale) {
        return "templates/reservation/reservation-" + parseLocale(locale) + ".html";
    }

    private String parseUserTemplateName(String locale) {
        return "templates/reservation/user-reservation-" + parseLocale(locale) + ".html";
    }

    private String parsePickedCoachTemplateName(String locale) {
        return "templates/reservation/picked-coach-" + parseLocale(locale) + ".html";
    }

    private String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }
}
