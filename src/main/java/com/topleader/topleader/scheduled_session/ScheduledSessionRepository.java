/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import java.time.LocalDateTime;
import java.util.List;

import com.topleader.topleader.coach.session.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;


/**
 * @author Daniel Slavik
 */
public interface ScheduledSessionRepository extends JpaSpecificationExecutor<ScheduledSession> , JpaRepository<ScheduledSession, Long>  {
    boolean existsByCoachUsernameAndTime(String coachUsername, LocalDateTime time);

    List<ScheduledSession> findAllByCoachUsernameAndTimeIsAfter(String coach, LocalDateTime time);

    List<ScheduledSession> findAllByUsernameAndTimeIsAfterAndIsPrivateIsFalse(String username, LocalDateTime now);
    List<ScheduledSession> findAllByUsernameAndTimeIsAfter(String username, LocalDateTime now);

    List<ScheduledSession> findAllByTimeBeforeAndPaidIsFalse(LocalDateTime time);



}
