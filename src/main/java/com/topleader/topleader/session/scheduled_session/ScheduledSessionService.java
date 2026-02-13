/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import com.topleader.topleader.common.metrics.MetricsService;
import com.topleader.topleader.session.user_allocation.UserAllocationService;
import org.springframework.jdbc.core.simple.JdbcClient;
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

    private final JdbcClient jdbcClient;


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

    public int markPendingSessionsAsCompleted() {
        var now = LocalDateTime.now();
        var threshold = now.minusHours(48);
        var count = scheduledSessionRepository.markPendingSessionsAsCompleted(threshold, now, "JOB");
        log.info("Marked {} sessions as COMPLETED (older than 48h)", count);
        return count;
    }

    public List<ScheduledSession> listUsersSessions(String username) {
        return scheduledSessionRepository.findAllByUsername(username);
    }

    public SessionSummary getSessionSummary(String username) {
        return jdbcClient.sql("""
                SELECT
                    COUNT(*) FILTER (WHERE status = 'UPCOMING' AND time > CURRENT_TIMESTAMP) AS upcoming,
                    COUNT(*) FILTER (WHERE status = 'UPCOMING' AND time <= CURRENT_TIMESTAMP) AS pending,
                    COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed,
                    COUNT(*) FILTER (WHERE status = 'NO_SHOW_CLIENT') AS no_show_client,
                    COALESCE((
                        SELECT SUM(ua.allocated_units) FROM user_allocation ua
                        JOIN coaching_package cp ON ua.package_id = cp.id
                        WHERE ua.username = :username AND ua.status = 'ACTIVE' AND cp.status = 'ACTIVE'
                    ), 0) AS allocated
                FROM scheduled_session
                WHERE username = :username
                """)
                .param("username", username)
                .query((rs, rowNum) -> {
                    var allocated = rs.getInt("allocated");
                    var upcoming = rs.getInt("upcoming");
                    var pending = rs.getInt("pending");
                    var completed = rs.getInt("completed");
                    var noShowClient = rs.getInt("no_show_client");
                    var consumed = pending + completed + noShowClient;
                    var remaining = Math.max(0, allocated - upcoming - consumed);
                    return new SessionSummary(allocated, upcoming, pending, completed, noShowClient, consumed, remaining);
                })
                .single();
    }

    public record SessionSummary(
            int allocatedUnits,
            int upcomingSessions,
            int pendingSessions,
            int completedSessions,
            int noShowClientSessions,
            int consumedUnits,
            int remainingUnits
    ) {}
}
