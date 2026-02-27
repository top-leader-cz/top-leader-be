package com.topleader.topleader.hr.program;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProgramControllerIT extends IntegrationTest {

    // ==================== GET /options ====================

    @Test
    @Sql("/sql/hr/program-options-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getOptions() throws Exception {
        mvc.perform(get("/api/latest/hr/programs/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(9)))
                .andExpect(jsonPath("$[?(@.key == 'opt.hr.session-attendance')].alwaysOn", contains(true)))
                .andExpect(jsonPath("$[?(@.key == 'opt.hr.session-attendance')].category", contains("HR")))
                .andExpect(jsonPath("$[?(@.key == 'opt.hr.micro-action-rate')].alwaysOn", contains(false)))
                .andExpect(jsonPath("$[?(@.key == 'opt.mgr.enrollment-status')].alwaysOn", contains(true)))
                .andExpect(jsonPath("$[?(@.key == 'opt.mgr.focus-area-goal')].alwaysOn", contains(false)));
    }

    // ==================== GET / (list programs) ====================

    @Test
    @Sql("/sql/hr/program-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void listPrograms() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, TestUtils.readFileAsString("hr/json/program-list-response.json"));
    }

    @Test
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void listPrograms_emptyWhenNoCompany() throws Exception {
        // hr_prog user doesn't exist yet (no @Sql) -> NotFoundException
        mvc.perform(get("/api/latest/hr/programs"))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /{programId} (detail) ====================

    @Test
    @Sql("/sql/hr/program-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getProgramDetail() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs/1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, TestUtils.readFileAsString("hr/json/program-detail-response.json"));
    }

    @Test
    @Sql("/sql/hr/program-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getProgramDetail_notFound() throws Exception {
        mvc.perform(get("/api/latest/hr/programs/999"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST / (create draft) ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void createDraft() throws Exception {
        mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "New Program",
                                    "goal": "Improve leadership",
                                    "targetGroup": "Team Leads",
                                    "durationDays": 90,
                                    "startDate": "2026-06-01T00:00:00",
                                    "milestoneDate": "2026-08-15T00:00:00",
                                    "focusAreas": ["fa.giving-feedback", "fa.delegation"],
                                    "participants": ["user1@test.cz"],
                                    "sessionsPerParticipant": 5,
                                    "recommendedCadence": "Every 2-3 weeks",
                                    "coachAssignmentModel": "PARTICIPANT_CHOOSES",
                                    "shortlistedCoaches": [],
                                    "microActionsEnabled": true,
                                    "enabledOptions": ["opt.hr.micro-action-rate"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("New Program"))
                .andExpect(jsonPath("$.goal").value("Improve leadership"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.durationDays").value(90))
                .andExpect(jsonPath("$.sessionsPerParticipant").value(5))
                .andExpect(jsonPath("$.coachAssignmentModel").value("PARTICIPANT_CHOOSES"))
                .andExpect(jsonPath("$.microActionsEnabled").value(true))
                .andExpect(jsonPath("$.focusAreas", hasSize(2)))
                .andExpect(jsonPath("$.enabledOptions", hasSize(1)));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void createDraft_minimalFields() throws Exception {
        mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Minimal Draft",
                                    "focusAreas": [],
                                    "participants": [],
                                    "shortlistedCoaches": [],
                                    "enabledOptions": []
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Minimal Draft"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.goal").doesNotExist())
                .andExpect(jsonPath("$.durationDays").value(0))
                .andExpect(jsonPath("$.sessionsPerParticipant").value(0))
                .andExpect(jsonPath("$.microActionsEnabled").value(false))
                .andExpect(jsonPath("$.coachAssignmentModel").value("PARTICIPANT_CHOOSES"));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void createDraft_validationError_blankName() throws Exception {
        mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "",
                                    "focusAreas": [],
                                    "participants": [],
                                    "shortlistedCoaches": [],
                                    "enabledOptions": []
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ==================== POST / (update draft) ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void updateDraft() throws Exception {
        mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": 1,
                                    "name": "Updated Program Name",
                                    "goal": "Updated goal",
                                    "targetGroup": "Senior Managers",
                                    "durationDays": 120,
                                    "milestoneDate": "2026-09-01T00:00:00",
                                    "focusAreas": ["fa.communication"],
                                    "participants": ["user1@test.cz", "user2@test.cz"],
                                    "sessionsPerParticipant": 8,
                                    "recommendedCadence": "Weekly",
                                    "coachAssignmentModel": "HR_ASSIGNS",
                                    "shortlistedCoaches": [],
                                    "microActionsEnabled": false,
                                    "enabledOptions": ["opt.hr.micro-action-rate", "opt.mgr.focus-area-goal"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Updated Program Name"))
                .andExpect(jsonPath("$.goal").value("Updated goal"))
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.durationDays").value(120))
                .andExpect(jsonPath("$.sessionsPerParticipant").value(8))
                .andExpect(jsonPath("$.coachAssignmentModel").value("HR_ASSIGNS"))
                .andExpect(jsonPath("$.microActionsEnabled").value(false))
                .andExpect(jsonPath("$.enabledOptions", hasSize(2)));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void updateDraft_notFound() throws Exception {
        mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "id": 999,
                                    "name": "Non-existent",
                                    "focusAreas": [],
                                    "participants": [],
                                    "shortlistedCoaches": [],
                                    "enabledOptions": []
                                }
                                """))
                .andExpect(status().isNotFound());
    }

    // ==================== GET /users ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getCompanyUsers_withProgramId() throws Exception {
        mvc.perform(get("/api/latest/hr/programs/users").param("programId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3))) // hr_prog + user1 + user2 (user3 is CANCELED)
                .andExpect(jsonPath("$[?(@.username == 'user1@test.cz')].added", contains(true)))
                .andExpect(jsonPath("$[?(@.username == 'user2@test.cz')].added", contains(false)));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getCompanyUsers_withoutProgramId() throws Exception {
        mvc.perform(get("/api/latest/hr/programs/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[?(@.added == true)]", hasSize(0)));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getCompanyUsers_invalidProgramId() throws Exception {
        mvc.perform(get("/api/latest/hr/programs/users").param("programId", "999"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /{programId}/launch ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/1/launch"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.name").value("Draft Program"));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_failsWithoutGoal() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/2/launch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("program.goal.required"));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_failsWithoutParticipants() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/3/launch"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$[0].errorCode").value("program.participants.required"));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_notFound() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/999/launch"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /recommend-coaches ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void recommendCoaches_emptyWhenNoCoaches() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/recommend-coaches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "goal": "Improve leadership",
                                    "focusAreas": ["fa.giving-feedback"],
                                    "targetGroup": "Team Leads"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void recommendCoaches_validationError_blankGoal() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/recommend-coaches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "goal": "",
                                    "focusAreas": ["fa.giving-feedback"]
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ==================== Security ====================

    @Test
    @WithMockUser(username = "regular_user", authorities = "USER")
    void accessDenied_forRegularUser() throws Exception {
        mvc.perform(get("/api/latest/hr/programs"))
                .andExpect(status().isForbidden());
    }

    @Test
    void accessDenied_forAnonymous() throws Exception {
        mvc.perform(get("/api/latest/hr/programs"))
                .andExpect(status().isUnauthorized());
    }
}
