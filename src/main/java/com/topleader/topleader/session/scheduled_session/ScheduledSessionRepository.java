package com.topleader.topleader.session.scheduled_session;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


/**
 * @author Daniel Slavik
 */
public interface ScheduledSessionRepository extends ListCrudRepository<ScheduledSession, Long> {
    boolean existsByCoachUsernameAndTime(String coachUsername, LocalDateTime time);

    @Query("SELECT * FROM scheduled_session WHERE coach_username = :coach AND time > :time AND status = 'UPCOMING'")
    List<ScheduledSession> findAllByCoachUsernameAndTimeIsAfterAndStatusUpcoming(String coach, LocalDateTime time);

    List<ScheduledSession> findAllByUsernameAndTimeIsAfterAndIsPrivateIsFalse(String username, LocalDateTime now);

    @Query("SELECT * FROM scheduled_session WHERE username = :username AND time > :time AND status = 'UPCOMING'")
    List<ScheduledSession> findAllByUsernameAndTimeIsAfterAndStatusUpcoming(String username, LocalDateTime time);

    List<ScheduledSession> findAllByTimeBeforeAndPaidIsFalse(LocalDateTime time);

    Optional<ScheduledSession> findByCoachUsernameAndId(String username, Long id);

    List<ScheduledSession> findAllByUsername(String username);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username IN (:usernames) AND status = 'UPCOMING' AND time >= CURRENT_TIMESTAMP")
    int countUpcomingByUsernames(List<String> usernames);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username IN (:usernames) AND status IN ('COMPLETED', 'NO_SHOW_CLIENT')")
    int countConsumedByUsernames(List<String> usernames);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username = :username AND status = 'UPCOMING' AND time > CURRENT_TIMESTAMP")
    int countUpcomingByUsername(String username);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username = :username AND status = 'COMPLETED'")
    int countCompletedByUsername(String username);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username = :username AND status = 'NO_SHOW_CLIENT'")
    int countNoShowClientByUsername(String username);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username IN (:usernames) AND status = 'UPCOMING' AND time >= :from AND time < :to AND time > CURRENT_TIMESTAMP")
    int countUpcomingByUsernamesAndTimeRange(List<String> usernames, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username IN (:usernames) AND status = 'COMPLETED' AND time >= :from AND time < :to")
    int countCompletedByUsernamesAndTimeRange(List<String> usernames, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username IN (:usernames) AND status = 'NO_SHOW_CLIENT' AND time >= :from AND time < :to")
    int countNoShowClientByUsernamesAndTimeRange(List<String> usernames, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username = :username AND status = 'UPCOMING' AND time >= :from AND time < :to AND time > CURRENT_TIMESTAMP")
    int countUpcomingByUsernameAndTimeRange(String username, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username = :username AND status = 'COMPLETED' AND time >= :from AND time < :to")
    int countCompletedByUsernameAndTimeRange(String username, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COUNT(*) FROM scheduled_session WHERE username = :username AND status = 'NO_SHOW_CLIENT' AND time >= :from AND time < :to")
    int countNoShowClientByUsernameAndTimeRange(String username, LocalDateTime from, LocalDateTime to);

    @Modifying
    @Query("UPDATE scheduled_session SET status = 'COMPLETED', updated_at = :now, updated_by = :updatedBy WHERE status = 'UPCOMING' AND time < :threshold")
    int markPendingSessionsAsCompleted(LocalDateTime threshold, LocalDateTime now, String updatedBy);

    long countByStatus(ScheduledSession.Status status);

}
