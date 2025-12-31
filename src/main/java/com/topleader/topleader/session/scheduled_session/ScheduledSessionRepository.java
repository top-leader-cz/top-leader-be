/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.session.scheduled_session;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
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

    Optional<ScheduledSession> findByCoachUsernameAndId(String username, Long id);

    List<ScheduledSession> findAllByUsername(String username);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username IN :usernames AND s.status = 'UPCOMING'")
    int countUpcomingByUsernames(List<String> usernames);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username IN :usernames AND s.status IN ('COMPLETED', 'NO_SHOW_CLIENT')")
    int countConsumedByUsernames(List<String> usernames);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username = :username AND s.status = 'UPCOMING'")
    int countUpcomingByUsername(String username);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username = :username AND s.status IN ('COMPLETED', 'NO_SHOW_CLIENT')")
    int countConsumedByUsername(String username);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username IN :usernames AND s.status = 'UPCOMING' AND s.time >= :from AND s.time < :to")
    int countUpcomingByUsernamesAndTimeRange(List<String> usernames, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username IN :usernames AND s.status IN ('COMPLETED', 'NO_SHOW_CLIENT') AND s.time >= :from AND s.time < :to")
    int countConsumedByUsernamesAndTimeRange(List<String> usernames, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username = :username AND s.status = 'UPCOMING' AND s.time >= :from AND s.time < :to")
    int countUpcomingByUsernameAndTimeRange(String username, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(s) FROM ScheduledSession s WHERE s.username = :username AND s.status IN ('COMPLETED', 'NO_SHOW_CLIENT') AND s.time >= :from AND s.time < :to")
    int countConsumedByUsernameAndTimeRange(String username, LocalDateTime from, LocalDateTime to);

    @Modifying
    @Query("UPDATE ScheduledSession s SET s.status = 'COMPLETED', s.updatedAt = :now WHERE (s.status = 'PENDING' OR s.status = 'UPCOMING') AND s.time < :threshold")
    int markPendingSessionsAsCompleted(LocalDateTime threshold, LocalDateTime now);

}
