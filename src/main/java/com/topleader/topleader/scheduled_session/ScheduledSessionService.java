/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import com.topleader.topleader.credit.CreditService;
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

    public void scheduleSession(ScheduledSession scheduledSession) {
        final var session =  scheduledSessionRepository.save(scheduledSession);
        creditService.scheduleSession(session.getId());
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
            .ifPresent(l -> l.forEach(s -> {
                creditService.cancelSession(s.getId());
                scheduledSessionRepository.delete(s);
            }));
    }

    public void cancelSession(Long sessionId) {
        scheduledSessionRepository.findById(sessionId).ifPresent(s -> {
            creditService.cancelSession(s.getId());
            scheduledSessionRepository.delete(s);
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
}
