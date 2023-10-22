/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static java.util.function.Predicate.not;


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

    public List<ScheduledSession> listCoachesFutureSessions(String username) {
        return scheduledSessionRepository.findAllByCoachUsernameAndTimeIsAfter(username, LocalDateTime.now());
    }

    public List<ScheduledSession> listUsersFutureSessions(String username) {
        return scheduledSessionRepository.findAllByUsernameAndTimeIsAfter(username, LocalDateTime.now());
    }

    public void deleteUserSessions(String username) {
        Optional.of(scheduledSessionRepository.findAllByUsernameAndTimeIsAfter(username, LocalDateTime.now()))
            .filter(not(List::isEmpty))
            .ifPresent(scheduledSessionRepository::deleteAll);
    }
}
