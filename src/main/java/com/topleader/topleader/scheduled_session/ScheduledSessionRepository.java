/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;


/**
 * @author Daniel Slavik
 */
public interface ScheduledSessionRepository extends JpaRepository<ScheduledSession, Long> {
    boolean existsByCoachUsernameAndTime(String coachUsername, LocalDateTime time);
}
