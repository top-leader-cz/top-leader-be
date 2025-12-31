/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.credit;

import com.topleader.topleader.session.scheduled_session.ScheduledSessionService;
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
public class CreditController {

    private final ScheduledSessionService scheduledSessionService;

    @PostMapping("/payments")
    @Secured({"JOB"})
    public void processPayments() {
        log.info("Triggering payment processing");
        scheduledSessionService.processPayments();
    }
}
