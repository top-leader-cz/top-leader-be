/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.email;

import com.topleader.topleader.common.calendar.ical.ICalService;
import com.topleader.topleader.common.exception.ApiValidationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.USER_NOT_FOUND;

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
            "en", "You've Been Selected as a Coach on TopLeader!",
            "cs", "Byli jste vybráni jako kouč na platformě TopLeader!",
            "fr", "Vous avez été sélectionné comme coach sur TopLeader!",
            "de", " Sie wurden als Coach auf TopLeader ausgewählt!");

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    private final UserLookup userLookup;

    private final Emailing emailService;

    private final Templating velocityService;

    private final ICalService iCalService;

    public void sendBookingAlertPrivateSessionEmail(SessionEmailData session) {
        log.info("Sending reservation alert for: [{}]", session);

        var user = lookupUser(session.username());
        var eventId = "session-" + session.id();

        emailService.sendEmail(
            user.email(),
            resolveSubject(subjectUserInvitations, user.locale()),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", user.firstName(),
                        "lastName", user.lastName(),
                        "time", session.time().toString(),
                        "link", appUrl)
                ),
                parseUserTemplateName(user.locale())
            ),
            iCalService.createCalendarPrivateEvent(
                session.time(),
                session.time().plusHours(1),
                user.email(),
                user.firstName() + " " + user.lastName(),
                PRIVATE_SESSION_EVENT_NAME,
                eventId
            )
        );
    }

    public void sendBookingAlertEmail(SessionEmailData session) {
        log.info("Sending reservation alert for: [{}]", session);

        var user = lookupUser(session.username());
        var coach = lookupUser(session.coachUsername());
        var eventId = "session-" + session.id();
        var meetLinkSection = buildMeetLinkSection(session.meetLink(), session.meetProviderLabel());

        var coachParams = new HashMap<>(Map.<String, Object>of(
                "firstName", coach.firstName(),
                "lastName", coach.lastName(),
                "link", appUrl,
                "meetLinkSection", meetLinkSection));

        emailService.sendEmail(
            coach.email(),
            resolveSubject(subjects, coach.locale()),
            velocityService.getMessage(coachParams, parseCoachTemplateName(coach.locale())),
            iCalService.createCalendarEvent(
                session.time(),
                session.time().plusHours(1),
                coach.email(),
                coach.firstName() + " " + coach.lastName(),
                user.email(),
                user.firstName() + " " + user.lastName(),
                "Session with " + user.firstName() + " " + user.lastName(),
                eventId,
                session.meetLink()
            )
        );

        var userParams = new HashMap<>(Map.<String, Object>of(
                "firstName", user.firstName(),
                "lastName", user.lastName(),
                "time", session.time().toString(),
                "link", appUrl,
                "meetLinkSection", meetLinkSection));

        emailService.sendEmail(
            user.email(),
            resolveSubject(subjectUserInvitations, user.locale()),
            velocityService.getMessage(userParams, parseUserTemplateName(user.locale())),
            iCalService.createCalendarEvent(
                session.time(),
                session.time().plusHours(1),
                coach.email(),
                coach.firstName() + " " + coach.lastName(),
                user.email(),
                user.firstName() + " " + user.lastName(),
                "Session with " + coach.firstName() + " " + coach.lastName(),
                eventId,
                session.meetLink()
            )
        );
    }

    private String buildMeetLinkSection(String meetLink, String providerLabel) {
        return Optional.ofNullable(meetLink)
                .filter(StringUtils::isNotBlank)
                .map(link -> {
                    var escaped = HtmlUtils.htmlEscape(link);
                    var label = Optional.ofNullable(providerLabel)
                            .filter(StringUtils::isNotBlank)
                            .orElse("Video Call");
                    return "<p>Join via " + HtmlUtils.htmlEscape(label) + ": <a href=\"" + escaped + "\" target=\"_blank\">" + escaped + "</a></p>";
                })
                .orElse(StringUtils.EMPTY);
    }

    public void sendCancelAlertEmail(SessionEmailData session) {
        log.info("Sending cancel alert for: [{}]", session);

        var user = lookupUser(session.username());
        var coach = lookupUser(session.coachUsername());
        var eventId = "session-" + session.id();

        emailService.sendEmail(
            coach.email(),
            resolveSubject(subjects, coach.locale()),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", coach.firstName(),
                        "lastName", coach.lastName(),
                        "link", appUrl)
                ),
                "templates/reservation/coach-cancel.html"),
            iCalService.cancelCalendarEvent(
                session.time(),
                session.time().plusHours(1),
                coach.email(),
                coach.firstName() + " " + coach.lastName(),
                user.email(),
                user.firstName() + " " + user.lastName(),
                "Session with " + user.firstName() + " " + user.lastName(),
                eventId
            )
        );

        emailService.sendEmail(
            user.email(),
            resolveSubject(subjects, user.locale()),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", user.firstName(),
                        "lastName", user.lastName(),
                        "time", session.time().toString(),
                        "link", appUrl)
                ),
                "templates/reservation/user-cancel.html"
            ),
            iCalService.cancelCalendarEvent(
                session.time(),
                session.time().plusHours(1),
                coach.email(),
                coach.firstName() + " " + coach.lastName(),
                user.email(),
                user.firstName() + " " + user.lastName(),
                "Session with " + coach.firstName() + " " + coach.lastName(),
                eventId
            )
        );
    }

    public void sendCancelAlertPrivateSessionEmail(SessionEmailData session) {
        log.info("Sending cancel alert for: [{}]", session);

        var user = lookupUser(session.username());
        var eventId = "session-" + session.id();

        emailService.sendEmail(
            user.email(),
            resolveSubject(subjects, user.locale()),
            velocityService.getMessage(
                new HashMap<>(
                    Map.of(
                        "firstName", user.firstName(),
                        "lastName", user.lastName(),
                        "time", session.time().toString(),
                        "link", appUrl)
                ),
                "templates/reservation/user-cancel.html"
            ),
            iCalService.cancelCalendarPrivateEvent(
                session.time(),
                session.time().plusHours(1),
                user.email(),
                user.firstName() + " " + user.lastName(),
                PRIVATE_SESSION_EVENT_NAME,
                eventId
            )
        );
    }

    public void sentPickedMessage(String coachUsername) {
        log.info("Sending coach picked email for coach: [{}]", coachUsername);

        var user = lookupUser(coachUsername);

        emailService.sendEmail(
                user.email(),
                resolveSubject(pickedCoachSubjects, user.locale()),
                velocityService.getMessage(
                        new HashMap<>(
                                Map.of(
                                        "firstName", user.firstName(),
                                        "lastName", user.lastName(),
                                        "link", appUrl)
                        ),
                        parsePickedCoachTemplateName(user.locale()))
               );
    }

    private String resolveSubject(Map<String, String> subjectMap, String locale) {
        return Optional.ofNullable(locale)
                .map(subjectMap::get)
                .orElse(subjectMap.getOrDefault(defaultLocale, "New Booking Alert on TopLeader"));
    }

    private UserLookup.EmailUser lookupUser(String username) {
        return userLookup.findByUsername(username)
                .orElseThrow(() -> new ApiValidationException(USER_NOT_FOUND, "username", username, "User not found " + username));
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
