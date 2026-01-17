package com.topleader.topleader.user.session.reminder;


import com.topleader.topleader.common.email.EmailService;
import com.topleader.topleader.common.email.TemplateService;
import com.topleader.topleader.user.session.UserActionStep;
import com.topleader.topleader.user.session.UserActionStepRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.util.StringUtils.parseLocale;


@Slf4j
@Service
@RequiredArgsConstructor
public class SessionReminderService {

    private static final Map<String, String> SUBJECTS = Map.of(
        "en", "Your Journey to Excellence Awaits, %s %s!",
        "cs", "Vaše cesta k rozvoji čeká, %s %s!",
        "de", "Ihr Weg zur Entwicklung wartet auf Sie, %s %s!",
        "fr", "Votre chemin vers le développement vous attend, %s %s!"
    );

    private final EmailService emailService;

    private final TemplateService velocityService;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    private final SessionReminderRepository sessionReminderRepository;

    private final UserActionStepRepository userActionStepRepository;

    public List<SessionReminderView> getUserWithNoScheduledSessions() {
        return sessionReminderRepository.findAll();
    }

    public void sendReminder(SessionReminderView user) {
        log.info("Sending reminder to user: {}", user.getUsername());

        final var subject = SUBJECTS.getOrDefault(user.getLocale(), defaultLocale).formatted(user.getFirstName(), user.getLastName());

        getShortTermGoal(user.getUsername()).ifPresentOrElse(
            goal -> emailService.sendEmail(
                user.getEmail(),
                subject,
                velocityService.getMessage(
                    new HashMap<>(
                        Map.of(
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "appLink", appUrl,
                            "shortTermGoal", goal)
                    ),
                    parseSessionReminderTemplate(user.getLocale(), true)
                )),
            () -> emailService.sendEmail(
                user.getEmail(),
                subject,
                velocityService.getMessage(
                    new HashMap<>(
                        Map.of(
                            "firstName", user.getFirstName(),
                            "lastName", user.getLastName(),
                            "appLink", appUrl)
                    ),
                    parseSessionReminderTemplate(user.getLocale(), false)
                )
            )
        );
    }

    private Optional<String> getShortTermGoal(String username) {
        return userActionStepRepository.findFirstByUsernameAndCheckedIsFalseOrderByDateDesc(username)
            .map(UserActionStep::getLabel);
    }

    public String parseSessionReminderTemplate(String locale, boolean hasShortTermGoal) {
        if (hasShortTermGoal) {
            return "templates/sessionReminder/reminder-with-short-term-" + parseLocale(locale) + ".html";
        } else {
            return "templates/sessionReminder/reminder-without-short-term-" + parseLocale(locale) + ".html";
        }
    }

}
