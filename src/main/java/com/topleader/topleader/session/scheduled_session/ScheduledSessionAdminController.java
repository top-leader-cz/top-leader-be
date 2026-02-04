/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/protected/jobs")
public class ScheduledSessionAdminController {

    private final ScheduledSessionService scheduledSessionService;

    @PostMapping("/mark-session-completed")
    @Secured({"JOB"})
    public MarkNoShowClientResponse markPendingSessionsAsNoShowClient() {
        log.info("Cron job: marking pending sessions older than 48h as COMPLETED");
        var count = scheduledSessionService.markPendingSessionsAsCompleted();
        log.info("Cron job: marked {} sessions as COMPLETED", count);
        return new MarkNoShowClientResponse(count);
    }

    public record MarkNoShowClientResponse(int markedCount) {}
}
