/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback_notification;

import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.feedback.entity.Recipient;
import com.topleader.topleader.feedback.repository.FeedbackFormRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.feedback_notification.FeedbackNotification.Status.PROCESSED;
import static java.util.function.Predicate.not;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/latest/feedback-notification")
public class FeedbackNotificationController {

    private final FeedbackNotificationService feedbackNotificationService;

    private final FeedbackFormRepository feedbackFormRepository;

    @Transactional
    @GetMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormNotificationDto getForm(
        @PathVariable long id,
        @AuthenticationPrincipal UserDetails user
    ) {

        return feedbackNotificationService.fetchForm(id)
            .filter(f -> f.getUsername().equalsIgnoreCase(user.getUsername()))
            .map(f -> FeedbackFormNotificationDto.fromEntity(f, isUnanswered(id)))
            .orElseThrow(NotFoundException::new);
    }

    @Transactional
    @PostMapping("/{id}/manual-reminder")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormNotificationDto triggerManualReminder(
        @PathVariable long id,
        @AuthenticationPrincipal UserDetails user
    ) {

        return feedbackNotificationService.fetchForm(id)
            .filter(f -> f.getUsername().equalsIgnoreCase(user.getUsername()))
            .map(feedbackNotificationService::triggerManualReminder)
            .map(f -> FeedbackFormNotificationDto.fromEntity(f, false))
            .orElseThrow(NotFoundException::new);
    }

    private boolean isUnanswered(Long formId) {
        return feedbackFormRepository.findById(formId)
            .map(f -> f.getRecipients().stream().anyMatch(not(Recipient::isSubmitted)))
            .orElseThrow(NotFoundException::new);
    }

    public record FeedbackFormNotificationDto(
        LocalDateTime feedbackFormEmailTime,
        LocalDateTime automaticReminderTime,
        LocalDateTime manualReminderAllowedAfter,
        Long feedbackFormId,
        LocalDateTime automaticReminderSentTime,
        boolean manualReminderAllowed,
        Status status
    ) {
        public enum Status {
            NEW,
            AUTOMATIC_REMINDER_SENT,
            MANUAL_SENT;

            public static Status from(FeedbackNotification.Status status) {
                return switch (status) {
                    case NEW -> NEW;
                    case PROCESSED -> AUTOMATIC_REMINDER_SENT;
                    case MANUAL_SENT -> MANUAL_SENT;
                };
            }

        }

        public static FeedbackFormNotificationDto fromEntity(FeedbackNotification entity, boolean isUnanswered) {


            return new FeedbackFormNotificationDto(
                entity.getCreatedAt(),
                entity.getNotificationTime(),
                entity.getManualAvailableAfter(),
                entity.getFeedbackFormId(),
                entity.getProcessedTime(),
                isUnanswered && PROCESSED.equals(entity.getStatus()),
                FeedbackFormNotificationDto.Status.from(entity.getStatus())
            );
        }
    }
}
