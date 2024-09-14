/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface ScheduledSessionRepository extends JpaRepository<ScheduledSession, Long> {
    boolean existsByCoachUsernameAndTime(String coachUsername, LocalDateTime time);

    List<ScheduledSession> findAllByCoachUsernameAndTimeIsAfter(String coach, LocalDateTime time);

    List<ScheduledSession> findAllByUsernameAndTimeIsAfterAndIsPrivateIsFalse(String username, LocalDateTime now);
    List<ScheduledSession> findAllByUsernameAndTimeIsAfter(String username, LocalDateTime now);

    List<ScheduledSession> findAllByTimeBeforeAndPaidIsFalse(LocalDateTime time);
}
