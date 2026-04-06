package com.topleader.topleader.program;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.session.user_allocation.UserAllocationRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProgramControllerIT extends IntegrationTest {

    @Autowired
    private UserAllocationRepository userAllocationRepository;

    // ==================== GET /options ====================

    @Test
    @Sql("/sql/hr/program-options-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getOptions() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs/options"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [
                    {"key": "opt.hr.session-attendance", "category": "HR", "alwaysOn": true},
                    {"key": "opt.hr.goal-completion", "category": "HR", "alwaysOn": true},
                    {"key": "opt.hr.micro-action-rate", "category": "HR", "alwaysOn": false},
                    {"key": "opt.hr.checkpoint-responses", "category": "HR", "alwaysOn": false},
                    {"key": "opt.hr.assessment-results", "category": "HR", "alwaysOn": false},
                    {"key": "opt.mgr.enrollment-status", "category": "MANAGER", "alwaysOn": true},
                    {"key": "opt.mgr.focus-area-goal", "category": "MANAGER", "alwaysOn": false},
                    {"key": "opt.mgr.session-attendance", "category": "MANAGER", "alwaysOn": false},
                    {"key": "opt.mgr.goal-progress", "category": "MANAGER", "alwaysOn": false}
                ]
                """);
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
        var result = mvc.perform(post("/api/latest/hr/programs")
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
                                    "participants": [{"username": "user1@test.cz", "managerUsername": null}],
                                    "sessionsPerParticipant": 5,
                                    "recommendedCadence": "Every 2-3 weeks",
                                    "coachAssignmentModel": "PARTICIPANT_CHOOSES",
                                    "shortlistedCoaches": [],
                                    "microActionsEnabled": true,
                                    "enabledOptions": ["opt.hr.micro-action-rate"],
                                    "coachLanguages": [],
                                    "coachCategories": []
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "name": "New Program",
                    "goal": "Improve leadership",
                    "status": "DRAFT",
                    "durationDays": 90,
                    "sessionsPerParticipant": 5,
                    "coachAssignmentModel": "PARTICIPANT_CHOOSES",
                    "microActionsEnabled": true,
                    "focusAreas": ["fa.giving-feedback", "fa.delegation"],
                    "enabledOptions": ["opt.hr.micro-action-rate"]
                }
                """);
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void createDraft_minimalFields() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Minimal Draft",
                                    "focusAreas": [],
                                    "participants": [],
                                    "shortlistedCoaches": [],
                                    "enabledOptions": [],
                                    "coachLanguages": [],
                                    "coachCategories": []
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "name": "Minimal Draft",
                    "status": "DRAFT",
                    "durationDays": 0,
                    "sessionsPerParticipant": 0,
                    "microActionsEnabled": false,
                    "coachAssignmentModel": "PARTICIPANT_CHOOSES"
                }
                """);
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
                                    "enabledOptions": [],
                                    "coachLanguages": [],
                                    "coachCategories": []
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== POST / (update draft) ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void updateDraft() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs")
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
                                    "participants": [{"username": "user1@test.cz", "managerUsername": null}, {"username": "user2@test.cz", "managerUsername": "hr_prog"}],
                                    "sessionsPerParticipant": 8,
                                    "recommendedCadence": "Weekly",
                                    "coachAssignmentModel": "HR_ASSIGNS",
                                    "shortlistedCoaches": [],
                                    "microActionsEnabled": false,
                                    "enabledOptions": ["opt.hr.micro-action-rate", "opt.mgr.focus-area-goal"],
                                    "coachLanguages": [],
                                    "coachCategories": []
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "id": 1,
                    "name": "Updated Program Name",
                    "goal": "Updated goal",
                    "status": "DRAFT",
                    "durationDays": 120,
                    "sessionsPerParticipant": 8,
                    "coachAssignmentModel": "HR_ASSIGNS",
                    "microActionsEnabled": false,
                    "enabledOptions": ["opt.hr.micro-action-rate", "opt.mgr.focus-area-goal"]
                }
                """);
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
                                    "enabledOptions": [],
                                    "coachLanguages": [],
                                    "coachCategories": []
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void createDraft_withManagerAssignment() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Program With Managers",
                                    "focusAreas": [],
                                    "participants": [
                                        {"username": "user1@test.cz", "managerUsername": "hr_prog"},
                                        {"username": "user2@test.cz", "managerUsername": null}
                                    ],
                                    "shortlistedCoaches": [],
                                    "enabledOptions": [],
                                    "coachLanguages": [],
                                    "coachCategories": []
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {"name": "Program With Managers", "status": "DRAFT"}
                """);
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void createDraft_withCycleLengthDays() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "name": "Cycle Program",
                                    "durationDays": 90,
                                    "cycleLengthDays": 30,
                                    "focusAreas": [],
                                    "participants": [],
                                    "shortlistedCoaches": [],
                                    "enabledOptions": [],
                                    "coachLanguages": [],
                                    "coachCategories": []
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "cycleLengthDays": 30,
                    "checkpoints": [
                        {"name": "Enrollment", "day": 0},
                        {"name": "Mid-cycle", "day": 15},
                        {"name": "Cycle review", "day": 30},
                        {"name": "Mid-cycle", "day": 45},
                        {"name": "Cycle review", "day": 60},
                        {"name": "Mid-cycle", "day": 75},
                        {"name": "Final review", "day": 90}
                    ]
                }
                """);
    }

    // ==================== GET /users ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getCompanyUsers_withProgramId() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs/users").param("programId", "1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [
                    {"username": "hr_prog", "added": false},
                    {"username": "user1@test.cz", "added": true},
                    {"username": "user2@test.cz", "added": false}
                ]
                """);
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getCompanyUsers_withoutProgramId() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs/users"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [
                    {"added": false},
                    {"added": false},
                    {"added": false}
                ]
                """);
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getCompanyUsers_invalidProgramId() throws Exception {
        mvc.perform(get("/api/latest/hr/programs/users").param("programId", "999"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== POST /{programId}/launch ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs/1/launch"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {"id": 1, "status": "CREATED", "name": "Draft Program"}
                """);

        var allocation = userAllocationRepository.findByPackageIdAndUsername(100L, "user1@test.cz").orElseThrow();
        assertThat(allocation.getAllocatedUnits()).isEqualTo(5);
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_failsWithoutGoal() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs/2/launch"))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [{"errorCode": "program.goal.required"}]
                """);
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_failsWithoutParticipants() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs/3/launch"))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [{"errorCode": "program.participants.required"}]
                """);
    }

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void launchProgram_notFound() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/999/launch"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== POST /recommend-coaches ====================

    @Test
    @Sql("/sql/hr/program-draft-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void recommendCoaches_emptyWhenNoCoaches() throws Exception {
        var result = mvc.perform(post("/api/latest/hr/programs/recommend-coaches")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "goal": "Improve leadership",
                                    "focusAreas": ["fa.giving-feedback"],
                                    "targetGroup": "Team Leads"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, "[]");
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
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== GET /coach-categories ====================

    @Test
    @Sql("/sql/hr/program-coach-preview-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getCoachCategories() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs/coach-categories"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                [
                    {"key": "ec.leadership", "name": "Leadership & Management"},
                    {"key": "ec.communication", "name": "Communication & Influence"},
                    {"key": "ec.resilience", "name": "Resilience & Wellbeing"}
                ]
                """);
    }

    // ==================== GET /focus-area-mappings ====================

    @Test
    @Sql("/sql/hr/program-coach-preview-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void getFocusAreaMappings() throws Exception {
        var result = mvc.perform(get("/api/latest/hr/programs/focus-area-mappings"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "fa.giving-feedback": "ec.communication",
                    "fa.delegation": "ec.leadership",
                    "fa.communication": "ec.communication"
                }
                """);
    }

    // ==================== POST /coach-preview ====================

    @Test
    @Sql("/sql/hr/program-coach-preview-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void coachPreview_exactMatchesOnly() throws Exception {
        stubAiResponse("rank ALL", """
                [{"username":"coach_en_lead","firstName":"Alice","lastName":"Leader","reason":"Best leadership match"}]
                """);

        var result = mvc.perform(post("/api/latest/hr/programs/coach-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "coachLanguages": ["en"],
                                    "coachCategories": ["ec.leadership"],
                                    "participantCount": 2,
                                    "goal": "Improve leadership",
                                    "focusAreas": ["fa.delegation"],
                                    "targetGroup": "Team Leads"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "exact": 1,
                    "coaches": [
                        {"username": "coach_en_lead", "matchType": "exact"}
                    ]
                }
                """);
    }

    @Test
    @Sql("/sql/hr/program-coach-preview-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void coachPreview_languageIsShowStopper() throws Exception {
        stubAiResponse("rank ALL", """
                [{"username":"coach_cs_lead","firstName":"Karel","lastName":"Vedouci","reason":"Czech leadership expert"}]
                """);

        var result = mvc.perform(post("/api/latest/hr/programs/coach-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "coachLanguages": ["cs"],
                                    "coachCategories": ["ec.leadership"],
                                    "participantCount": 2,
                                    "goal": "Improve leadership",
                                    "focusAreas": ["fa.delegation"],
                                    "targetGroup": "Team Leads"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "exact": 1,
                    "coaches": [
                        {"username": "coach_cs_lead", "matchType": "exact"}
                    ]
                }
                """);
    }

    @Test
    @Sql("/sql/hr/program-coach-preview-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void coachPreview_multipleCategories() throws Exception {
        stubAiResponse("rank ALL", """
                [{"username":"coach_en_lead","firstName":"Alice","lastName":"Leader","reason":"Top match"},
                 {"username":"coach_en_comm","firstName":"Bob","lastName":"Communicator","reason":"Strong comms"}]
                """);

        var result = mvc.perform(post("/api/latest/hr/programs/coach-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "coachLanguages": ["en"],
                                    "coachCategories": ["ec.leadership", "ec.communication"],
                                    "participantCount": 4,
                                    "goal": "Improve all",
                                    "focusAreas": ["fa.delegation", "fa.communication"],
                                    "targetGroup": "Everyone"
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "coaches": [
                        {"username": "coach_en_lead", "matchType": "exact"},
                        {"username": "coach_en_comm", "matchType": "exact"}
                    ]
                }
                """);
    }

    @Test
    @Sql("/sql/hr/program-coach-preview-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void coachPreview_validationError_blankGoal() throws Exception {
        mvc.perform(post("/api/latest/hr/programs/coach-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "coachLanguages": ["en"],
                                    "coachCategories": ["ec.leadership"],
                                    "participantCount": 4,
                                    "goal": "",
                                    "focusAreas": ["fa.delegation"]
                                }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @Sql("/sql/hr/program-coach-preview-test.sql")
    @WithMockUser(username = "hr_prog", authorities = "HR")
    void coachPreview_noCategorySelected_aiRecommendsFromLanguagePool() throws Exception {
        stubAiResponse("rank ALL", """
                [{"username":"coach_en_lead","firstName":"Alice","lastName":"Leader","reason":"Strong leadership background"},
                 {"username":"coach_en_comm","firstName":"Bob","lastName":"Communicator","reason":"Great communicator"}]
                """);

        var result = mvc.perform(post("/api/latest/hr/programs/coach-preview")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "coachLanguages": ["en"],
                                    "coachCategories": [],
                                    "participantCount": 4,
                                    "goal": "Improve leadership",
                                    "focusAreas": ["fa.delegation"]
                                }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "exact": 0,
                    "coaches": [
                        {"username": "coach_en_lead", "matchType": "recommended"},
                        {"username": "coach_en_comm", "matchType": "recommended"}
                    ]
                }
                """);
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
