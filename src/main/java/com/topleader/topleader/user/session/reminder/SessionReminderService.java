package com.topleader.topleader.user.session.reminder;


import com.topleader.topleader.email.EmailService;
import com.topleader.topleader.email.EmailTemplateService;
import com.topleader.topleader.email.VelocityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionReminderService {

    private static final Map<String, String> subjectUserInvitations = Map.of(
            "en", "Keep Your Progress on Track!",
            "cs", "Udržujte svůj pokrok na správné cestě!",
            "fr", "Maintenez votre progression sur la bonne voie!",
            "de", " Halten Sie Ihren Fortschritt auf Kurs!");

    private final EmailService emailService;

    private final VelocityService velocityService;

    private final EmailTemplateService emailTemplateService;

    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;

    private final SessionReminderRepository sessionReminderRepository;

    public List<SessionReminderView> getUserWithNoScheduledSessions() {
        return sessionReminderRepository.findAll();
    }

    public void sendReminder(SessionReminderView user) {
        log.info("Sending reminder to user: {}", user.getUsername());
        emailService.sendEmail(
                user.getUsername(),
                subjectUserInvitations.getOrDefault(user.getLocale(), defaultLocale),
                velocityService.getMessage(
                        new HashMap<>(
                                Map.of(
                                        "firstName", user.getFirstName(),
                                        "lastName", user.getLastName(),
                                        "link", appUrl)
                        ),
                        emailTemplateService.parseSessionReminder(user.getLocale())
                ));
    }

}
