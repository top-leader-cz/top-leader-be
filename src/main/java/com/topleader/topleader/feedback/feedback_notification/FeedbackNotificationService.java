package com.topleader.topleader.feedback.feedback_notification;

import com.topleader.topleader.common.email.EmailService;
import com.topleader.topleader.common.email.TemplateService;
import com.topleader.topleader.common.exception.NotFoundException;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.common.util.transaction.TransactionService;
import com.topleader.topleader.user.UserRepository;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


import static com.topleader.topleader.feedback.feedback_notification.FeedbackNotification.Status.*;
import static com.topleader.topleader.common.util.common.CommonUtils.TOP_LEADER_FORMATTER;
import static java.time.ZoneOffset.UTC;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FeedbackNotificationService {

    private static final int DAYS_TO_NOTIFY = 5;
    private static final int DAYS_TO_MANUAL = 3;

    private static final Map<String, String> subjects = Map.of(
        "en", "Friendly Reminder: Please Share Your Feedback for %s %s",
        "cs", "Přátelská připomínka: Prosím, podělte se o svou zpětnou vazbu pro %s %s",
        "fr", "Rappel amical : Merci de partager vos retours pour %s %s",
        "de", "Freundliche Erinnerung: Bitte teilen Sie Ihr Feedback für %s %s mit");


    private final FeedbackNotificationRepository feedbackNotificationRepository;

    private final FeedbackFormRepository feedbackFormRepository;

    private final RecipientRepository recipientRepository;

    private final TransactionService transactionService;

    private final EmailService emailService;

    private final TemplateService velocityService;

    private final UserRepository userRepository;


    @Value("${top-leader.app-url}")
    private String appUrl;

    @Value("${top-leader.supported-invitations}")
    private List<String> supportedInvitations;

    @Value("${top-leader.default-locale}")
    private String defaultLocale;


    public Optional<FeedbackNotification> fetchForm(long formId) {
        return feedbackNotificationRepository.findByFeedbackFormId(formId);
    }

    public void processFeedbackNotification() {
        feedbackNotificationRepository.findAllByStatusAndNotificationTimeBefore(NEW, LocalDateTime.now())
            .forEach(this::processSingleNotificationTx);
    }

    private void processSingleNotificationTx(FeedbackNotification feedbackNotification) {
        transactionService.execute(() -> processSingleNotification(feedbackNotification));
    }

    private void processSingleNotification(FeedbackNotification feedbackNotificationEntity) {
        try {

            final var freshNotification = feedbackNotificationRepository.findById(feedbackNotificationEntity.getId())
                .orElseThrow(NotFoundException::new);

            final var feedbackForm = feedbackFormRepository.findById(freshNotification.getFeedbackFormId())
                .orElseThrow(NotFoundException::new);

            final var user = userRepository.findByUsername(feedbackForm.getUsername()).orElseThrow(NotFoundException::new);

            recipientRepository.findByFormId(feedbackForm.getId()).stream()
                .filter(r -> !r.isSubmitted())
                .forEach(r -> {
                    var feedbackLink = String.format("%s/#/feedback/%s/%s/%s", appUrl, feedbackForm.getId(), r.getRecipient(), r.getToken());
                    var params = Map.of(
                        "validTo", feedbackForm.getValidTo().format(TOP_LEADER_FORMATTER),
                        "link", feedbackLink,
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName());
                    var body = velocityService.getMessage(new HashMap<>(params), parseTemplateName(user.getLocale()));
                    var subject = String.format(subjects.getOrDefault(user.getLocale(), defaultLocale),
                        user.getFirstName(),
                        user.getLastName());

                    emailService.sendEmail(r.getRecipient(), subject, body);
                });


            feedbackNotificationRepository.save(feedbackNotificationEntity
                .setStatus(PROCESSED)
                .setProcessedTime(OffsetDateTime.now(UTC).toLocalDateTime())
                .setManualAvailableAfter(LocalDateTime.now(UTC).plusDays(DAYS_TO_MANUAL).withNano(0).withSecond(0).withMinute(0))
            );

        } catch (Exception e) {
            log.error("Error processing feedback notification with id " + feedbackNotificationEntity.getId(), e);
        }

    }

    private void processManualNotification(FeedbackNotification feedbackNotificationEntity) {
        try {

            final var freshNotification = feedbackNotificationRepository.findById(feedbackNotificationEntity.getId())
                .orElseThrow(NotFoundException::new);

            final var feedbackForm = feedbackFormRepository.findById(freshNotification.getFeedbackFormId())
                .orElseThrow(NotFoundException::new);

            final var user = userRepository.findByUsername(feedbackForm.getUsername()).orElseThrow(NotFoundException::new);

            recipientRepository.findByFormId(feedbackForm.getId()).stream()
                .filter(r -> !r.isSubmitted())
                .forEach(r -> {
                    var feedbackLink = String.format("%s/#/feedback/%s/%s/%s", appUrl, feedbackForm.getId(), r.getRecipient(), r.getToken());
                    var params = Map.of(
                        "validTo", feedbackForm.getValidTo().format(TOP_LEADER_FORMATTER),
                        "link", feedbackLink,
                        "firstName", user.getFirstName(),
                        "lastName", user.getLastName());
                    var body = velocityService.getMessage(new HashMap<>(params), parseTemplateName(user.getLocale()));
                    var subject = String.format(subjects.getOrDefault(user.getLocale(), defaultLocale),
                        user.getFirstName(),
                        user.getLastName());

                    emailService.sendEmail(r.getRecipient(), subject, body);
                });


            feedbackNotificationRepository.save(feedbackNotificationEntity
                .setStatus(MANUAL_SENT)
                .setManualReminderSentTime(OffsetDateTime.now(UTC).toLocalDateTime())
            );

        } catch (Exception e) {
            log.error("Error processing feedback notification with id " + feedbackNotificationEntity.getId(), e);
        }

    }


    public String parseTemplateName(String locale) {
        return "templates/feedbackNotification/feedback-notification-" + parseLocale(locale) + ".html";
    }

    public String parseLocale(String locale) {
        return supportedInvitations.contains(locale) ? locale : defaultLocale;
    }

    public void registerNotification(long formId) {

        final var feedback = feedbackFormRepository.findById(formId).orElseThrow();

        feedbackNotificationRepository.save(
            feedbackNotificationRepository.findByFeedbackFormId(formId)
                .map(n -> n
                    .setStatus(NEW)
                    .setProcessedTime(null)
                    .setNotificationTime(LocalDateTime.now()
                        .withMinute(0)
                        .withSecond(0)
                        .withNano(0)
                        .plusDays(DAYS_TO_NOTIFY))
                ).orElse(
                    new FeedbackNotification()
                        .setFeedbackFormId(formId)
                        .setUsername(feedback.getUsername())
                        .setStatus(NEW)
                        .setNotificationTime(
                            LocalDateTime.now()
                                .withMinute(0)
                                .withSecond(0)
                                .withNano(0)
                                .plusDays(DAYS_TO_NOTIFY)
                        )
                        .setCreatedAt(LocalDateTime.now(UTC))
                )
        );
    }

    public FeedbackNotification triggerManualReminder(FeedbackNotification f) {
        processManualNotification(f);
        return feedbackNotificationRepository.findById(f.getId()).orElseThrow();
    }
}
