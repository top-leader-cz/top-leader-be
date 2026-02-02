/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import com.topleader.topleader.common.metrics.MetricsService;
import com.topleader.topleader.session.user_allocation.UserAllocationService;
import org.springframework.transaction.annotation.Transactional;
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

    private final UserAllocationService userAllocationService;

    private final MetricsService metrics;


    public boolean isAlreadyScheduled(String coach, LocalDateTime date) {
        return scheduledSessionRepository.existsByCoachUsernameAndTime(coach, date);
    }

    @Transactional
    public ScheduledSession scheduleSession(ScheduledSession scheduledSession, String createdBy) {
        var now = LocalDateTime.now();
        scheduledSession.setCreatedAt(now);
        scheduledSession.setUpdatedAt(now);
        scheduledSession.setUpdatedBy(createdBy);
        userAllocationService.consumeUnit(scheduledSession.getUsername());
        var session = scheduledSessionRepository.save(scheduledSession);
        metrics.incrementSessionScheduled();
        return session;
    }

    public List<ScheduledSession> listCoachesFutureSessions(String username) {
        return scheduledSessionRepository.findAllByCoachUsernameAndTimeIsAfterAndStatusUpcoming(username, LocalDateTime.now());
    }

    public List<ScheduledSession> listUsersFutureSessions(String username) {
        return scheduledSessionRepository.findAllByUsernameAndTimeIsAfterAndStatusUpcoming(username, LocalDateTime.now());
    }

    public Optional<ScheduledSession> getFutureSession(Long sessionId) {
        return scheduledSessionRepository.findById(sessionId)
            .filter(s -> s.getTime().isAfter(LocalDateTime.now()))
            ;
    }

    public void deleteUserCoachedSessions(String username) {
        Optional.of(scheduledSessionRepository.findAllByUsernameAndTimeIsAfterAndIsPrivateIsFalse(username, LocalDateTime.now()))
            .filter(not(List::isEmpty))
            .ifPresent(l -> l.forEach(s -> {
                userAllocationService.releaseUnit(s.getUsername());
                scheduledSessionRepository.delete(s);
            }));
    }

    public void cancelSession(Long sessionId, String canceledBy) {
        cancelSession(sessionId, ScheduledSession.Status.CANCELED_BY_CLIENT, canceledBy);
    }

    public void cancelSessionByCoach(Long sessionId, String canceledBy) {
        cancelSession(sessionId, ScheduledSession.Status.CANCELED_BY_COACH, canceledBy);
    }

    private void cancelSession(Long sessionId, ScheduledSession.Status status, String canceledBy) {
        scheduledSessionRepository.findById(sessionId).ifPresent(s -> {
            userAllocationService.releaseUnit(s.getUsername());
            s.setStatus(status);
            s.setUpdatedAt(LocalDateTime.now());
            s.setUpdatedBy(canceledBy);
            scheduledSessionRepository.save(s);
        });
    }

    public Optional<ScheduledSession> getSessionData(Long sessionId) {
        return scheduledSessionRepository.findById(sessionId);
    }

    public int markPendingSessionsAsNoShowClient() {
        var now = LocalDateTime.now();
        var threshold = now.minusHours(48);
        var count = scheduledSessionRepository.markPendingSessionsAsCompleted(threshold, now, "JOB");
        log.info("Marked {} sessions as COMPLETED (older than 48h)", count);
        return count;
    }

    public List<ScheduledSession> listUsersSessions(String username) {
        return scheduledSessionRepository.findAllByUsername(username);
    }
}
