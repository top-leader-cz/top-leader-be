/*
 * Copyright (c) 2024 Price f(x), s.r.o.
 */
package com.topleader.topleader.feedback_notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@RequestMapping("/api/protected/jobs")
@RequiredArgsConstructor
public class TriggerNotificationJobController {

    private final FeedbackNotificationService feedbackNotificationService;

    @PostMapping("/feedback-notification")
    @Secured({"JOB"})
    public void processPayments() {
        log.info("Triggering feedback notification processing");
        feedbackNotificationService.processFeedbackNotification();
    }
}
