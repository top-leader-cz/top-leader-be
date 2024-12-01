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

    private static final Map<String, String> subjectUserInvitations = Map.ofEntries(
            Map.entry("DAYS3_en", "Keep Your Progress on Track!"),
            Map.entry("DAYS10_en", "Ready to Take the Next Step in Your Development?"),
            Map.entry("DAYS24_en", "Don’t Lose Momentum—Book Your Next Session!"),
            Map.entry("DAYS3_cs", "Udržujte svůj pokrok na správné cestě!"),
            Map.entry("DAYS10_cs", "Připraveni na další krok ve svém rozvoji?"),
            Map.entry("DAYS24_cs", "Neztrácejte tempo – naplánujte si další lekci!"),
            Map.entry("DAYS3_fr", "Maintenez votre progression sur la bonne voie!"),
            Map.entry("DAYS10_fr", "Prêt(e) à franchir la prochaine étape de votre développement?"),
            Map.entry("DAYS24_fr", "Ne perdez pas votre élan – Réservez votre prochaine session !"),
            Map.entry("DAYS3_de", "Halten Sie Ihren Fortschritt auf Kurs!"),
            Map.entry("DAYS10_de", "Bereit für den nächsten Schritt in Ihrer Entwicklung?"),
            Map.entry("DAYS24_de", "Verlieren Sie nicht den Schwung – Buchen Sie Ihre nächste Sitzung!")
        );

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
                subjectUserInvitations.getOrDefault(String.join("_", user.getReminderInterval().name(), user.getLocale()), defaultLocale),
                velocityService.getMessage(
                        new HashMap<>(
                                Map.of(
                                        "firstName", user.getFirstName(),
                                        "lastName", user.getLastName(),
                                        "link", appUrl)
                        ),
                        emailTemplateService.parseSessionReminder(user.getReminderInterval(), user.getLocale())
                ));
    }

}
