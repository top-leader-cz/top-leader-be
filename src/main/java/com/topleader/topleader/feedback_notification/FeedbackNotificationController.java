/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback_notification;

import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.user.UserRepository;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.util.common.user.UserUtils.getUserTimeZoneId;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/latest/feedback-notification")
@RequiredArgsConstructor
public class FeedbackNotificationController {

    private final FeedbackNotificationService feedbackNotificationService;

    private final UserRepository userRepository;

    @Transactional
    @GetMapping("/{id}")
    @Secured({"ADMIN", "HR", "COACH", "USER"})
    public FeedbackFormNotificationDto getForm(
        @PathVariable long id,
        @AuthenticationPrincipal UserDetails user
        ) {

        final var userZoneId = getUserTimeZoneId(userRepository.findById(user.getUsername()));


        return feedbackNotificationService.fetchForm(id)
            .filter(f -> f.getUsername().equalsIgnoreCase(user.getUsername()))
            .map(f -> FeedbackFormNotificationDto.fromEntity(f, userZoneId))
            .orElseThrow(NotFoundException::new);
    }


    public record FeedbackFormNotificationDto(
        LocalDateTime notificationTime,
        Long feedbackFormId,
        LocalDateTime processedTime,
        Status status
    ) {
        public enum Status {
            NEW,
            PROCESSED
            ;

            public static Status from(FeedbackNotification.Status status) {
                return switch (status) {
                    case NEW -> NEW;
                    case PROCESSED -> PROCESSED;
                };
            }

        }

        public static FeedbackFormNotificationDto fromEntity(FeedbackNotification entity, ZoneId userZoneId) {


            return new FeedbackFormNotificationDto(
                entity.getNotificationTime()
                    .atZone(ZoneOffset.UTC)
                    .withZoneSameInstant(userZoneId)
                    .toLocalDateTime(),
                entity.getFeedbackFormId(),
                Optional.ofNullable(entity.getProcessedTime())
                    .map(p -> p.atZone(ZoneOffset.UTC).withZoneSameInstant(userZoneId))
                    .map(ZonedDateTime::toLocalDateTime)
                    .orElse(null),
                FeedbackFormNotificationDto.Status.from(entity.getStatus())
            );
        }
    }
}
