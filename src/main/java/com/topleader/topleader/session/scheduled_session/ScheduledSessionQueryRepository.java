package com.topleader.topleader.session.scheduled_session;

import lombok.AllArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class ScheduledSessionQueryRepository {

    private final JdbcClient jdbcClient;

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
