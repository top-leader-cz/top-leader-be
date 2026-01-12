/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import com.topleader.topleader.credit.CreditService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
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

    private final CreditService creditService;

    public boolean isAlreadyScheduled(String coach, LocalDateTime date) {
        return scheduledSessionRepository.existsByCoachUsernameAndTime(coach, date);
    }

    public boolean isPossibleToSchedule(String user, String coach) {
        return creditService.canScheduleSession(user, coach);
    }

    public ScheduledSession scheduleSession(ScheduledSession scheduledSession, String createdBy) {
        var now = LocalDateTime.now();
        scheduledSession.setCreatedAt(now);
        scheduledSession.setUpdatedAt(now);
        scheduledSession.setUpdatedBy(createdBy);
        final var session = scheduledSessionRepository.save(scheduledSession);
        creditService.scheduleSession(session.getId());
        return session;
    }

    public List<ScheduledSession> listCoachesFutureSessions(String username) {
        return scheduledSessionRepository.findAllByCoachUsernameAndTimeIsAfterAndStatusUpcoming(username, LocalDateTime.now());
    }

    public List<ScheduledSession> listUsersFutureSessions(String username) {
        return scheduledSessionRepository.findAllByUsernameAndTimeIsAfter(username, LocalDateTime.now());
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
                creditService.cancelSession(s.getId());
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
            creditService.cancelSession(s.getId());
            s.setStatus(status);
            s.setUpdatedAt(LocalDateTime.now());
            s.setUpdatedBy(canceledBy);
            scheduledSessionRepository.save(s);
        });
    }

    @Async
    public void processPayments() {
        final var paymentTime = LocalDateTime.now().plusDays(1);
        scheduledSessionRepository.findAllByTimeBeforeAndPaidIsFalse(paymentTime)
            .forEach(s -> {
                try {
                    creditService.paySession(s.getId());
                } catch (Exception e) {
                    log.error("Unable to process payment for session " + s.getId(), e);
                }
            });
    }

    public Optional<ScheduledSession> getSessionData(Long sessionId) {
        return scheduledSessionRepository.findById(sessionId);
    }

    @Transactional
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
