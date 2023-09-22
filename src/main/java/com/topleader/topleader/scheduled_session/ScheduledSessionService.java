/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@AllArgsConstructor
public class ScheduledSessionService {

    private final ScheduledSessionRepository scheduledSessionRepository;

    public boolean isAlreadyScheduled(String coach, LocalDateTime date) {
        return scheduledSessionRepository.existsByCoachUsernameAndTime(coach, date);
    }

    public ScheduledSession scheduleSession(ScheduledSession scheduledSession) {
        return scheduledSessionRepository.save(scheduledSession);
    }
}
