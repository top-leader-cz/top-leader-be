/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.scheduled_session;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/sql/scheduled_session/scheduled-session-admin-test.sql")
class ScheduledSessionAdminControllerIT extends IntegrationTest {

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    private ScheduledSession createSession(LocalDateTime time, ScheduledSession.Status status, boolean paid) {
        var now = LocalDateTime.now();
        return scheduledSessionRepository.save(new ScheduledSession()
                .setUsername("user1")
                .setCoachUsername("coach")
                .setTime(time)
                .setStatus(status)
                .setPaid(paid)
                .setPrivate(false)
                .setCreatedAt(now)
                .setUpdatedAt(now));
    }

    @Test
    @WithMockUser(authorities = {"JOB"})
    void markSessionCompleted_marksUpcomingOlderThan48h() throws Exception {
        var now = LocalDateTime.now();

        // UPCOMING session older than 48h - should be marked
        var upcomingOldId = createSession(now.minusHours(49), ScheduledSession.Status.UPCOMING, false).getId();

        // Another UPCOMING session older than 48h - should be marked
        var upcomingOld2Id = createSession(now.minusHours(50), ScheduledSession.Status.UPCOMING, false).getId();

        // Future UPCOMING session - should NOT be marked
        var upcomingFutureId = createSession(now.plusDays(1), ScheduledSession.Status.UPCOMING, false).getId();

        // COMPLETED session - should NOT be changed
        var completedId = createSession(now.minusHours(60), ScheduledSession.Status.COMPLETED, true).getId();

        mvc.perform(post("/api/protected/jobs/mark-session-completed"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.markedCount").value(2));

        // Verify old UPCOMING was marked as COMPLETED
        assertThat(scheduledSessionRepository.findById(upcomingOldId))
                .isPresent()
                .hasValueSatisfying(s -> assertThat(s.getStatus()).isEqualTo(ScheduledSession.Status.COMPLETED));

        // Verify second old UPCOMING was marked as COMPLETED
        assertThat(scheduledSessionRepository.findById(upcomingOld2Id))
                .isPresent()
                .hasValueSatisfying(s -> assertThat(s.getStatus()).isEqualTo(ScheduledSession.Status.COMPLETED));

        // Verify future UPCOMING was NOT changed
        assertThat(scheduledSessionRepository.findById(upcomingFutureId))
                .isPresent()
                .hasValueSatisfying(s -> assertThat(s.getStatus()).isEqualTo(ScheduledSession.Status.UPCOMING));

        // Verify already COMPLETED was NOT changed
        assertThat(scheduledSessionRepository.findById(completedId))
                .isPresent()
                .hasValueSatisfying(s -> assertThat(s.getStatus()).isEqualTo(ScheduledSession.Status.COMPLETED));
    }

    @Test
    @WithMockUser(authorities = {"JOB"})
    void markSessionCompleted_noSessionsToMark_returnsZero() throws Exception {
        // Only create a future session
        createSession(LocalDateTime.now().plusDays(1), ScheduledSession.Status.UPCOMING, false);

        mvc.perform(post("/api/protected/jobs/mark-session-completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.markedCount").value(0));
    }

    @Test
    @WithMockUser(username = "regularUser", authorities = {"USER"})
    void markSessionCompleted_regularUserDenied() throws Exception {
        mvc.perform(post("/api/protected/jobs/mark-session-completed"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void markSessionCompleted_hrUserDenied() throws Exception {
        mvc.perform(post("/api/protected/jobs/mark-session-completed"))
                .andExpect(status().isForbidden());
    }
}
