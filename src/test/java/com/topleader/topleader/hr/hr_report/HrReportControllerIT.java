/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.hr.hr_report;

import com.topleader.topleader.IntegrationTest;

import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(scripts = "/sql/hr_report/hr-report-test.sql")
class HrReportControllerIT extends IntegrationTest {

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    private void createSession(String username, LocalDateTime time, ScheduledSession.Status status) {
        var now = LocalDateTime.now();
        scheduledSessionRepository.save(new ScheduledSession()
                .setUsername(username)
                .setCoachUsername("coach")
                .setTime(time)
                .setStatus(status)
                .setPaid(status != ScheduledSession.Status.UPCOMING)
                .setPrivate(false)
                .setCreatedAt(now)
                .setUpdatedAt(now));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getHrReport_asHr_success() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/1/hr-report"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.packageInfo.id").value(1))
                .andExpect(jsonPath("$.packageInfo.companyId").value(1))
                .andExpect(jsonPath("$.packageInfo.poolType").value("CORE"))
                .andExpect(jsonPath("$.packageInfo.status").value("ACTIVE"))
                .andExpect(jsonPath("$.summary.totalUnits").value(100))
                .andExpect(jsonPath("$.summary.allocatedUnits").value(35)) // user1=10 + user2=20 + canceledWithAlloc=5
                .andExpect(jsonPath("$.summary.plannedSessions").value(0))
                .andExpect(jsonPath("$.summary.pendingSessions").value(0))
                .andExpect(jsonPath("$.summary.completedSessions").value(0))
                .andExpect(jsonPath("$.rows.length()").value(7)); // All users in company except CANCELED without allocation
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getHrReport_withSessions_calculatesMetrics() throws Exception {
        createSession("user1", LocalDateTime.now().plusDays(1), ScheduledSession.Status.UPCOMING);  // planned
        createSession("user1", LocalDateTime.now().minusDays(1), ScheduledSession.Status.UPCOMING); // pending
        createSession("user1", LocalDateTime.now().minusDays(2), ScheduledSession.Status.COMPLETED);
        createSession("user2", LocalDateTime.now().minusDays(3), ScheduledSession.Status.NO_SHOW_CLIENT);

        mvc.perform(get("/api/latest/coaching-packages/1/hr-report"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.summary.plannedSessions").value(1))
                .andExpect(jsonPath("$.summary.pendingSessions").value(1))
                .andExpect(jsonPath("$.summary.completedSessions").value(1))
                .andExpect(jsonPath("$.summary.consumedUnits").value(3)); // pending + completed + noShow
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getHrReport_perUserMetrics() throws Exception {
        createSession("user1", LocalDateTime.now().plusDays(1), ScheduledSession.Status.UPCOMING);  // planned
        createSession("user1", LocalDateTime.now().minusDays(1), ScheduledSession.Status.UPCOMING); // pending
        createSession("user1", LocalDateTime.now().minusDays(2), ScheduledSession.Status.COMPLETED);

        mvc.perform(get("/api/latest/coaching-packages/1/hr-report"))
                .andExpect(status().isOk())
                .andDo(print())
                .andExpect(jsonPath("$.rows[?(@.userId == 'user1')].plannedSessions").value(1))
                .andExpect(jsonPath("$.rows[?(@.userId == 'user1')].pendingSessions").value(1))
                .andExpect(jsonPath("$.rows[?(@.userId == 'user1')].completedSessions").value(1))
                .andExpect(jsonPath("$.rows[?(@.userId == 'user1')].allocatedUnits").value(10));
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getHrReport_deniedForOtherCompany() throws Exception {
        // Package 3 belongs to company 2
        mvc.perform(get("/api/latest/coaching-packages/3/hr-report"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "adminUser", authorities = {"ADMIN"})
    void getHrReport_adminCanAccessAnyPackage() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/3/hr-report"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageInfo.companyId").value(2));
    }

    @Test
    @WithMockUser(username = "regularUser", authorities = {"USER"})
    void getHrReport_regularUserDenied() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/1/hr-report"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getHrReport_excludesCanceledWithoutAllocation_includesCanceledWithAllocation() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/1/hr-report"))
                .andExpect(status().isOk())
                .andDo(print())
                // canceledWithAlloc has allocated_units > 0 -> visible
                .andExpect(jsonPath("$.rows[?(@.userId == 'canceledWithAlloc')]").exists())
                // canceledNoAlloc has no allocation -> hidden
                .andExpect(jsonPath("$.rows[?(@.userId == 'canceledNoAlloc')]").doesNotExist());
    }

    @Test
    @WithMockUser(username = "hrUser", authorities = {"HR"})
    void getHrReport_withDateParams() throws Exception {
        mvc.perform(get("/api/latest/coaching-packages/1/hr-report")
                        .param("from", "2024-01-01T00:00:00.000Z")
                        .param("to", "2024-12-31T23:59:59.000Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.packageInfo.id").value(1));
    }
}
