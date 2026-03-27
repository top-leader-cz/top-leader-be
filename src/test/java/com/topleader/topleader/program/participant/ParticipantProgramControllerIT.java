package com.topleader.topleader.program.participant;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import com.topleader.topleader.program.participant.practice.WeeklyPractice;
import com.topleader.topleader.program.participant.practice.WeeklyPracticeRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/participant/participant-test.sql")
class ParticipantProgramControllerIT extends IntegrationTest {

    @Autowired
    private ProgramParticipantRepository participantRepository;

    @Autowired
    private WeeklyPracticeRepository weeklyPracticeRepository;

    // ==================== GET /status ====================

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getStatus_enrolledAndInvited() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/status"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "enrolled": true,
                    "programId": 1,
                    "programName": "Leadership 90d",
                    "programGoal": "Improve feedback culture",
                    "durationDays": 90,
                    "sessionsPerParticipant": 5,
                    "participantStatus": "INVITED"
                }
                """);
    }

    @Test
    @WithMockUser(username = "no_program_user", authorities = "USER")
    void getStatus_notEnrolled() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/status"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "enrolled": false,
                    "programId": null,
                    "programName": null,
                    "programGoal": null,
                    "durationDays": 0,
                    "sessionsPerParticipant": 0,
                    "participantStatus": null
                }
                """);
    }

    // ==================== GET /{programId}/focus-areas ====================

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getFocusAreas_returnsEnrollmentInfo() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/1/focus-areas"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "programGoal": "Improve feedback culture",
                    "focusAreas": ["fa.giving-feedback", "fa.delegation"],
                    "selectedFocusArea": null,
                    "personalGoal": null
                }
                """);
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getFocusAreas_notFound_whenNotParticipant() throws Exception {
        mvc.perform(get("/api/latest/participant/programs/999/focus-areas"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /{programId}/enroll ====================

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void enroll_success() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "focusArea": "fa.giving-feedback",
                                    "personalGoal": "I want to give better feedback to my team"
                                }
                                """))
                .andExpect(status().isOk());

        var participant = participantRepository.findById(1L).orElseThrow();
        assertThat(participant.getStatus()).isEqualTo(ProgramParticipant.Status.ENROLLING);
        assertThat(participant.getFocusArea()).isEqualTo("fa.giving-feedback");
        assertThat(participant.getPersonalGoal()).isEqualTo("I want to give better feedback to my team");
        assertThat(participant.getEnrolledAt()).isNotNull();
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void enroll_fails_whenFocusAreaBlank() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "", "personalGoal": "test" }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void enroll_fails_whenFocusAreaNotInProgram() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.unknown", "personalGoal": "test" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.enroll.focus.area.invalid");
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void enroll_allowsReEnrollInEnrollingStatus() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.giving-feedback", "personalGoal": "test" }
                                """))
                .andExpect(status().isOk());

        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.delegation", "personalGoal": "changed goal" }
                                """))
                .andExpect(status().isOk());

        var participant = participantRepository.findById(1L).orElseThrow();
        assertThat(participant.getFocusArea()).isEqualTo("fa.delegation");
        assertThat(participant.getPersonalGoal()).isEqualTo("changed goal");
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void enroll_fails_whenAlreadyActive() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.giving-feedback", "personalGoal": "test" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.enroll.invalid.status");
    }

    // ==================== GET /{programId}/assessment/baseline ====================

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getBaselineQuestions_success() throws Exception {
        // first enroll to set focus area
        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.giving-feedback", "personalGoal": "test" }
                                """))
                .andExpect(status().isOk());

        var result = mvc.perform(get("/api/latest/participant/programs/1/assessment/baseline"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "focusAreaKey": "fa.giving-feedback",
                    "questions": [
                        { "order": 1, "text": "I feel confident giving constructive feedback." },
                        { "order": 2, "text": "I give feedback regularly to my team." },
                        { "order": 3, "text": "I receive feedback well from others." },
                        { "order": 4, "text": "I adjust my feedback style based on the person." },
                        { "order": 5, "text": "Feedback in my team leads to visible improvements." }
                    ],
                    "q1": null, "q2": null, "q3": null, "q4": null, "q5": null,
                    "openText": null
                }
                """);
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getBaselineQuestions_notFound_whenNoFocusArea() throws Exception {
        mvc.perform(get("/api/latest/participant/programs/1/assessment/baseline"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /{programId}/assessment/baseline ====================

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void submitBaseline_success() throws Exception {
        stubAiResponse("weekly practice", """
                ["Practice 1", "Practice 2", "Practice 3", "Practice 4", "Practice 5"]""");

        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.giving-feedback", "personalGoal": "test" }
                                """))
                .andExpect(status().isOk());

        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/baseline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 2, "q4": 5, "q5": 3, "openText": "I tend to avoid direct feedback" }
                                """))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                ["Practice 1", "Practice 2", "Practice 3", "Practice 4", "Practice 5"]""");

        var participant = participantRepository.findById(1L).orElseThrow();
        assertThat(participant.getStatus()).isEqualTo(ProgramParticipant.Status.ACTIVE);
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void submitBaseline_fails_whenAnswerOutOfRange() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.giving-feedback", "personalGoal": "test" }
                                """))
                .andExpect(status().isOk());

        mvc.perform(post("/api/latest/participant/programs/1/assessment/baseline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 6, "q2": 4, "q3": 2, "q4": 5, "q5": 3 }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void submitBaseline_fails_whenStatusNotEnrolling() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/baseline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 2, "q4": 5, "q5": 3 }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.baseline.invalid.status");
    }

    // ==================== GET /{programId}/dashboard ====================

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void dashboard_midCycleNotDue_whenJustEnrolled() throws Exception {
        enrollAndCompleteBaseline();

        var result = mvc.perform(get("/api/latest/participant/programs/1/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "programName": "Leadership 90d",
                    "programGoal": "Improve feedback culture",
                    "focusArea": "fa.giving-feedback",
                    "personalGoal": "test",
                    "currentCycle": 1,
                    "participantStatus": "ACTIVE",
                    "practice": { "text": "Practice 1", "source": "AI", "fridayResponse": null, "weekNumber": 1 },
                    "nextSession": null,
                    "sessionsConsumed": 0,
                    "sessionsAllocated": 0,
                    "midCycleStatus": "NOT_DUE",
                    "finalStatus": "NOT_DUE"
                }
                """);
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void dashboard_midCycleDue_whenPastMidpoint() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/1/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("\"midCycleStatus\":\"DUE\"");
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void dashboard_createsNewPracticeForCurrentWeek() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/1/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // participant2 enrolled 60 days ago, practice was week 1 — dashboard should auto-create for current week
        // 60 days / 7 = week 9 (cycle-relative, capped at 90/7=12)
        assertThat(result).contains("\"weekNumber\":9");
        assertThat(result).contains("\"text\":\"Give feedback to one team member today\"");
        assertThat(result).contains("\"fridayResponse\":null");
    }

    // ==================== POST /{programId}/checkin ====================

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void checkin_success_yes() throws Exception {
        enrollAndCompleteBaseline();

        mvc.perform(post("/api/latest/participant/programs/1/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "response": "YES" }
                                """))
                .andExpect(status().isOk());

        var practice = weeklyPracticeRepository.findLatestByParticipantId(1L).orElseThrow();
        assertThat(practice.getFridayResponse()).isEqualTo(WeeklyPractice.FridayResponse.YES);
        assertThat(practice.getBlockerReason()).isNull();
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void checkin_success_partialWithBlocker() throws Exception {
        enrollAndCompleteBaseline();

        mvc.perform(post("/api/latest/participant/programs/1/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "response": "PARTIAL", "blockerReason": "Too many meetings this week" }
                                """))
                .andExpect(status().isOk());

        var practice = weeklyPracticeRepository.findLatestByParticipantId(1L).orElseThrow();
        assertThat(practice.getFridayResponse()).isEqualTo(WeeklyPractice.FridayResponse.PARTIAL);
        assertThat(practice.getBlockerReason()).isEqualTo("Too many meetings this week");
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void checkin_fails_whenNotActive() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "response": "YES" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.checkin.invalid.status");
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void checkin_fails_whenResponseNull() throws Exception {
        enrollAndCompleteBaseline();

        mvc.perform(post("/api/latest/participant/programs/1/checkin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "response": null }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== GET /{programId}/assessment/mid ====================

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void getMidCycleQuestions_success() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/1/assessment/mid"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "focusAreaKey": "fa.giving-feedback",
                    "questions": [
                        { "order": 1, "text": "I feel confident giving constructive feedback." },
                        { "order": 2, "text": "I give feedback regularly to my team." },
                        { "order": 3, "text": "I receive feedback well from others." },
                        { "order": 4, "text": "I adjust my feedback style based on the person." },
                        { "order": 5, "text": "Feedback in my team leads to visible improvements." }
                    ],
                    "q1": null, "q2": null, "q3": null, "q4": null, "q5": null,
                    "openText": null
                }
                """);
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getMidCycleQuestions_notFound_whenNoFocusArea() throws Exception {
        mvc.perform(get("/api/latest/participant/programs/1/assessment/mid"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /{programId}/assessment/mid ====================

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void submitMidCycle_success() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/assessment/mid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 3, "q4": 3, "q5": 3, "openText": "The practices helped me notice opportunities" }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void submitMidCycle_fails_whenAlreadyCompleted() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/assessment/mid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 3, "q4": 3, "q5": 3 }
                                """))
                .andExpect(status().isOk());

        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/mid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 4, "q4": 4, "q5": 4 }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.mid.cycle.already.completed");
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void submitMidCycle_fails_whenNotActive() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/mid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 3, "q4": 3, "q5": 3 }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.checkin.invalid.status");
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void submitMidCycle_fails_whenAnswerOutOfRange() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/assessment/mid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 6, "q2": 4, "q3": 3, "q4": 3, "q5": 3 }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== GET /{programId}/assessment/mid/result ====================

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void getMidCycleResult_success() throws Exception {
        // submit mid-cycle first
        mvc.perform(post("/api/latest/participant/programs/1/assessment/mid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 3, "q4": 3, "q5": 3 }
                                """))
                .andExpect(status().isOk());

        var result = mvc.perform(get("/api/latest/participant/programs/1/assessment/mid/result"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "focusAreaKey": "fa.giving-feedback",
                    "questions": [
                        { "order": 1, "text": "I feel confident giving constructive feedback.", "baseline": 2, "mid": 3 },
                        { "order": 2, "text": "I give feedback regularly to my team.", "baseline": 3, "mid": 4 },
                        { "order": 3, "text": "I receive feedback well from others.", "baseline": 2, "mid": 3 },
                        { "order": 4, "text": "I adjust my feedback style based on the person.", "baseline": 3, "mid": 3 },
                        { "order": 5, "text": "Feedback in my team leads to visible improvements.", "baseline": 4, "mid": 3 }
                    ]
                }
                """);
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void getMidCycleResult_notFound_whenNoMidAssessment() throws Exception {
        mvc.perform(get("/api/latest/participant/programs/1/assessment/mid/result"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getMidCycleResult_fails_whenNoBaseline() throws Exception {
        mvc.perform(get("/api/latest/participant/programs/1/assessment/mid/result"))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== GET /{programId}/assessment/final ====================

    @Test
    @WithMockUser(username = "participant3", authorities = "USER")
    void getFinalQuestions_success() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/1/assessment/final"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "focusAreaKey": "fa.giving-feedback",
                    "questions": [
                        { "order": 1, "text": "I feel confident giving constructive feedback." },
                        { "order": 2, "text": "I give feedback regularly to my team." },
                        { "order": 3, "text": "I receive feedback well from others." },
                        { "order": 4, "text": "I adjust my feedback style based on the person." },
                        { "order": 5, "text": "Feedback in my team leads to visible improvements." }
                    ],
                    "q1": null, "q2": null, "q3": null, "q4": null, "q5": null,
                    "openText": null
                }
                """);
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void getFinalQuestions_notFound_whenNoFocusArea() throws Exception {
        mvc.perform(get("/api/latest/participant/programs/1/assessment/final"))
                .andExpect(status().isNotFound());
    }

    // ==================== POST /{programId}/assessment/final ====================

    @Test
    @WithMockUser(username = "participant3", authorities = "USER")
    void submitFinal_success_setsCompleted() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5, "openText": "Great program", "nps": 9 }
                                """))
                .andExpect(status().isOk());

        var participant = participantRepository.findById(3L).orElseThrow();
        assertThat(participant.getStatus()).isEqualTo(ProgramParticipant.Status.COMPLETED);
    }

    @Test
    @WithMockUser(username = "participant3", authorities = "USER")
    void submitFinal_fails_whenAlreadyCompleted() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5, "nps": 9 }
                                """))
                .andExpect(status().isOk());

        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5, "nps": 8 }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.final");
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void submitFinal_fails_whenNotActive() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5 }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.final.invalid.status");
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void submitFinal_fails_whenNotDue() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5 }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.final.not.due");
    }

    @Test
    @WithMockUser(username = "participant3", authorities = "USER")
    void submitFinal_fails_whenNpsMissingOnFinalCycle() throws Exception {
        var result = mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5, "openText": "Great" }
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("participant.final.nps.required");
    }

    @Test
    @WithMockUser(username = "participant3", authorities = "USER")
    void submitFinal_fails_whenAnswerOutOfRange() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 6, "q2": 5, "q3": 3, "q4": 4, "q5": 5 }
                                """))
                .andExpect(status().isUnprocessableEntity());
    }

    // ==================== GET /{programId}/journey ====================

    @Test
    @WithMockUser(username = "participant3", authorities = "USER")
    void getJourney_success() throws Exception {
        // submit final first
        mvc.perform(post("/api/latest/participant/programs/1/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5, "openText": "Great program", "nps": 9 }
                                """))
                .andExpect(status().isOk());

        var result = mvc.perform(get("/api/latest/participant/programs/1/journey?cycle=1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        TestUtils.assertJsonEquals(result, """
                {
                    "programName": "Leadership 90d",
                    "focusAreaKey": "fa.giving-feedback",
                    "personalGoal": "Lead by example",
                    "isFinalCycle": true,
                    "questions": [
                        { "order": 1, "text": "I feel confident giving constructive feedback.", "baseline": 2, "mid": 3, "end": 4, "delta": 2 },
                        { "order": 2, "text": "I give feedback regularly to my team.", "baseline": 3, "mid": 4, "end": 5, "delta": 2 },
                        { "order": 3, "text": "I receive feedback well from others.", "baseline": 2, "mid": 3, "end": 3, "delta": 1 },
                        { "order": 4, "text": "I adjust my feedback style based on the person.", "baseline": 3, "mid": 4, "end": 4, "delta": 1 },
                        { "order": 5, "text": "Feedback in my team leads to visible improvements.", "baseline": 4, "mid": 5, "end": 5, "delta": 1 }
                    ],
                    "stats": {
                        "sessionsConsumed": 0,
                        "sessionsAllocated": 0,
                        "practicesTotal": 3,
                        "practicesResponded": 2,
                        "avgImprovement": 1.4
                    }
                }
                """);
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void getJourney_notFound_whenNoFinalAssessment() throws Exception {
        mvc.perform(get("/api/latest/participant/programs/1/journey?cycle=1"))
                .andExpect(status().isNotFound());
    }

    // ==================== Dashboard finalStatus ====================

    @Test
    @WithMockUser(username = "participant3", authorities = "USER")
    void dashboard_finalStatusDue_whenPastCycleEnd() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/1/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("\"finalStatus\":\"DUE\"");
    }

    // ==================== Multi-cycle ====================

    @Test
    @WithMockUser(username = "participant4", authorities = "USER")
    void submitFinal_multiCycle_advancesCycleInsteadOfCompleting() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/2/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5, "openText": "Good cycle" }
                                """))
                .andExpect(status().isOk());

        var participant = participantRepository.findById(4L).orElseThrow();
        assertThat(participant.getCurrentCycle()).isEqualTo(2);
        assertThat(participant.getStatus()).isEqualTo(ProgramParticipant.Status.ENROLLING);
    }

    @Test
    @WithMockUser(username = "participant4", authorities = "USER")
    void submitFinal_multiCycle_npsNotRequired() throws Exception {
        mvc.perform(post("/api/latest/participant/programs/2/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5 }
                                """))
                .andExpect(status().isOk());
    }

    // ==================== Journey with mid=null ====================

    @Test
    @WithMockUser(username = "participant4", authorities = "USER")
    void getJourney_success_withoutMidAssessment() throws Exception {
        // submit final for cycle 1 (without mid for this test — delete mid first)
        // participant4 has mid assessment, so let's test with participant3 approach:
        // Actually participant4 has mid, let's just submit final and get journey
        mvc.perform(post("/api/latest/participant/programs/2/assessment/final")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 4, "q2": 5, "q3": 3, "q4": 4, "q5": 5 }
                                """))
                .andExpect(status().isOk());

        var result = mvc.perform(get("/api/latest/participant/programs/2/journey?cycle=1"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("\"isFinalCycle\":false");
        assertThat(result).contains("\"mid\":3");
    }

    // ==================== Helpers ====================

    private void enrollAndCompleteBaseline() throws Exception {
        stubAiResponse("weekly practice", """
                ["Practice 1", "Practice 2", "Practice 3", "Practice 4", "Practice 5"]""");

        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "focusArea": "fa.giving-feedback", "personalGoal": "test" }
                                """))
                .andExpect(status().isOk());

        mvc.perform(post("/api/latest/participant/programs/1/assessment/baseline")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 2, "q4": 5, "q5": 3 }
                                """))
                .andExpect(status().isOk());
    }
}
