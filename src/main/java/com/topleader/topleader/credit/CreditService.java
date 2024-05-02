/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.credit;

import com.topleader.topleader.coach.Coach;
import com.topleader.topleader.coach.CoachRepository;
import com.topleader.topleader.coach.rate.CoachRate;
import com.topleader.topleader.coach.rate.CoachRateRepository;
import com.topleader.topleader.credit.history.CreditHistory;
import com.topleader.topleader.credit.history.CreditHistoryRepository;
import com.topleader.topleader.exception.NotFoundException;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.user.UserRepository;
import com.topleader.topleader.util.ObjectMapperUtils;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * @author Daniel Slavik
 */
@Service
@RequiredArgsConstructor
public class CreditService {

    private final UserRepository userRepository;

    private final CoachRepository coachRepository;

    private final CreditHistoryRepository creditHistoryRepository;

    private final ScheduledSessionRepository scheduledSessionRepository;

    private final CoachRateRepository coachRateRepository;

    public boolean canScheduleSession(String username, String coach) {
        final var coachRate = coachRepository.findById(coach)
            .map(Coach::getRate)
            .flatMap(coachRateRepository::findById)
            .map(CoachRate::getRateCredit)
            .orElseThrow();
        final var user = userRepository.findById(username).orElseThrow();

        if (coach.equals(user.getFreeCoach())) {
            return true;
        }

        return Optional.ofNullable(user.getScheduledCredit()).orElse(0) + coachRate <= Optional.ofNullable(user.getCredit()).orElse(0);
    }

    @Transactional
    public void topUpCredit(String username) {
        final var user = userRepository.findById(username).orElseThrow(NotFoundException::new);

        final var credit = Optional.ofNullable(user.getRequestedCredit()).orElse(0);

        userRepository.save(
            user
                .setCredit(Optional.ofNullable(user.getCredit()).orElse(0) + credit)
                .setSumRequestedCredit(Optional.ofNullable(user.getSumRequestedCredit()).orElse(0) + credit)
                .setRequestedCredit(0)
        );
        creditHistoryRepository.save(
            new CreditHistory()
                .setTime(LocalDateTime.now())
                .setUsername(user.getUsername())
                .setType(CreditHistory.Type.ADDED)
                .setCredit(credit)
        );
    }

    @Transactional
    public void scheduleSession(Long sessionId) {
        final var session = scheduledSessionRepository.findById(sessionId).orElseThrow(NotFoundException::new);

        if (session.isPaid()) {
            return;
        }

        final var coachRate = coachRepository.findById(session.getCoachUsername())
            .map(Coach::getRate)
            .flatMap(coachRateRepository::findById)
            .map(CoachRate::getRateCredit)
            .orElseThrow(NotFoundException::new);
        final var user = userRepository.findById(session.getUsername()).orElseThrow(NotFoundException::new);

        if (Optional.ofNullable(user.getScheduledCredit()).orElse(0) + coachRate > Optional.ofNullable(user.getCredit()).orElse(0)) {
            throw new IllegalStateException("Not enough credit");
        }

        user.setScheduledCredit(Optional.ofNullable(user.getScheduledCredit()).orElse(0) + coachRate);

        userRepository.save(user);
        creditHistoryRepository.save(
            new CreditHistory()
                .setTime(LocalDateTime.now())
                .setUsername(user.getUsername())
                .setType(CreditHistory.Type.SCHEDULED)
                .setCredit(coachRate)
                .setContext(ObjectMapperUtils.toJsonString(session))
        );
    }

    @Transactional
    public void cancelSession(Long sessionId) {
        final var session = scheduledSessionRepository.findById(sessionId).orElseThrow(NotFoundException::new);

        if (session.isPaid()) {
            return;
        }

        final var coachRate = coachRepository.findById(session.getCoachUsername())
            .map(Coach::getRate)
            .flatMap(coachRateRepository::findById)
            .map(CoachRate::getRateCredit)
            .orElseThrow();
        final var user = userRepository.findById(session.getUsername()).orElseThrow();

        user.setScheduledCredit(Optional.ofNullable(user.getScheduledCredit()).orElse(0) - coachRate);

        userRepository.save(user);
        creditHistoryRepository.save(
                new CreditHistory()
                    .setTime(LocalDateTime.now())
                    .setUsername(user.getUsername())
                    .setType(CreditHistory.Type.CANCELED)
                    .setCredit(coachRate)
                    .setContext(ObjectMapperUtils.toJsonString(session))
        );
    }

    @Transactional
    public void paySession(Long sessionId) {
        final var session = scheduledSessionRepository.findById(sessionId).orElseThrow(NotFoundException::new);

        if (session.isPaid()) {
            return;
        }

        session.setPaid(true);

        final var coachRate = coachRepository.findById(session.getCoachUsername())
            .map(Coach::getRate)
            .flatMap(coachRateRepository::findById)
            .map(CoachRate::getRateCredit)
            .orElseThrow();
        final var coach = userRepository.findById(session.getCoachUsername()).orElseThrow(NotFoundException::new);
        final var user = userRepository.findById(session.getUsername()).orElseThrow(NotFoundException::new);

        user
            .setCredit(Optional.ofNullable(user.getCredit()).orElse(0) - coachRate)
            .setScheduledCredit(Optional.ofNullable(user.getScheduledCredit()).orElse(0) - coachRate)
            .setPaidCredit(Optional.ofNullable(user.getPaidCredit()).orElse(0) + coachRate)
        ;
        coach.setCredit(Optional.ofNullable(coach.getCredit()).orElse(0) + coachRate);



        scheduledSessionRepository.save(session);
        userRepository.saveAll(List.of(user, coach));

        final var now = LocalDateTime.now();
        creditHistoryRepository.saveAll(
            List.of(
                new CreditHistory()
                    .setTime(now)
                    .setUsername(user.getUsername())
                    .setType(CreditHistory.Type.PAID)
                    .setCredit(coachRate)
                    .setContext(ObjectMapperUtils.toJsonString(session)),
                new CreditHistory()
                    .setTime(now)
                    .setUsername(coach.getUsername())
                    .setType(CreditHistory.Type.RECEIVED)
                    .setCredit(coachRate)
                    .setContext(ObjectMapperUtils.toJsonString(session))
            )
        );
    }
}
