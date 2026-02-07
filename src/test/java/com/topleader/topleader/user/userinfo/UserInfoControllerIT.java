package com.topleader.topleader.user.userinfo;

import com.icegreen.greenmail.util.GreenMailUtil;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.common.ai.AiPrompt;
import com.topleader.topleader.common.ai.AiPromptService;
import com.topleader.topleader.session.user_allocation.UserAllocationRepository;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.StrengthStoredData;
import com.topleader.topleader.history.data.ValuesStoredData;
import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.user.UserRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.user.userinsight.UserInsightRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = {"/sql/user_info/user-info-test.sql"})
class UserInfoControllerIT extends IntegrationTest {

    @Autowired
    private DataHistoryRepository dataHistoryRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ScheduledSessionRepository scheduledSessionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserAllocationRepository userAllocationRepository;

    @Autowired
    AiClient aiClient;

    @Autowired
    UserInsightRepository userInsightRepository;

    private ScheduledSession createSession(String username, String coachUsername, LocalDateTime time, boolean paid, boolean isPrivate) {
        var now = LocalDateTime.now();
        return new ScheduledSession()
                .setPaid(paid)
                .setPrivate(isPrivate)
                .setUsername(username)
                .setCoachUsername(coachUsername)
                .setTime(time)
                .setCreatedAt(now)
                .setUpdatedAt(now);
    }

    private ScheduledSession createSession(String username, String coachUsername, LocalDateTime time, boolean paid, boolean isPrivate, ScheduledSession.Status status) {
        return createSession(username, coachUsername, time, paid, isPrivate).setStatus(status);
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void getEmptyDetailTest() throws Exception {

        mvc.perform(get("/api/latest/user-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.firstName", is("Some")))
            .andExpect(jsonPath("$.lastName", is("Dude")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.userRoles", hasSize(1)))
            .andExpect(jsonPath("$.userRoles", hasItems("USER")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
            .andExpect(jsonPath("$.locale",is("en")))
            .andExpect(jsonPath("$.companyId", is(100)))
        ;
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void getNotEmptyDetailTest() throws Exception {

        mvc.perform(get("/api/latest/user-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user2")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.strengths", hasItems("s1", "s2")))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.values", hasItems("v1", "v2")))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasItems("a1", "a2")))
            .andExpect(jsonPath("$.locale", is("en")))
            .andExpect(jsonPath("$.companyId", nullValue()))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setLocaleTest() throws Exception {

        mvc.perform(post("/api/latest/user-info/locale")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "locale": "fr"
                     }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
            .andExpect(jsonPath("$.locale", is("fr")))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setTimezoneTest() throws Exception {

        mvc.perform(post("/api/latest/user-info/timezone")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "timezone": "CST"
                     }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.timeZone", is("CST")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setNotes() throws Exception {

        mvc.perform(post("/api/latest/user-info/notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "notes": "some cool notes"
                     }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes", is("some cool notes")))
        ;
    }

    @Test
    @WithMockUser(username = "user2", authorities = "USER")
    void getNotes() throws Exception {

        mvc.perform(get("/api/latest/user-info/notes"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes", is("cool note")))
        ;
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setStrengthsTest() throws Exception {

        mvc.perform(post("/api/latest/user-info/strengths")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "data": ["v1", "v2"]
                     }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.strengths", hasItems("v1", "v2")))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
        ;

        final var userInfoData = userInfoRepository.findByUsername("user").orElseThrow();

        assertIterableEquals(List.of("v1", "v2"), userInfoData.getStrengths());

        final var data = dataHistoryRepository.findByUsernameAndType("user", DataHistory.Type.STRENGTHS.name());

        assertEquals(1, data.size());
        final var storedHistoryData = data.get(0);

        assertEquals(DataHistory.Type.STRENGTHS, storedHistoryData.getType());
        assertEquals("user", storedHistoryData.getUsername());
        assertEquals(StrengthStoredData.class, storedHistoryData.getData().getClass());
        assertIterableEquals(List.of("v1", "v2"), ((StrengthStoredData)storedHistoryData.getData()).getStrengths());
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void setValuesTest() throws Exception {

        mvc.perform(post("/api/latest/user-info/values")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "data": ["v1", "v2"]
                     }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.values", hasSize(2)))
            .andExpect(jsonPath("$.values", hasItems("v1", "v2")))
            .andExpect(jsonPath("$.strengths", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
        ;

        final var userInfoData = userInfoRepository.findByUsername("user").orElseThrow();

        assertIterableEquals(List.of("v1", "v2"), userInfoData.getValues());

        final var data = dataHistoryRepository.findByUsernameAndType("user", DataHistory.Type.VALUES.name());

        assertEquals(1, data.size());
        final var storedHistoryData = data.get(0);

        assertEquals(DataHistory.Type.VALUES, storedHistoryData.getType());
        assertEquals("user", storedHistoryData.getUsername());
        assertEquals(ValuesStoredData.class, storedHistoryData.getData().getClass());
        assertIterableEquals(List.of("v1", "v2"), ((ValuesStoredData)storedHistoryData.getData()).getValues());
    }

    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void deleteCoachTest() throws Exception {

        scheduledSessionRepository.saveAll(List.of(
            createSession("user_with_coach", "coach", LocalDateTime.now().plusHours(3), false, false),
            createSession("user_with_coach", "coach", LocalDateTime.now().plusDays(3), false, false),
            createSession("user_with_coach", null, LocalDateTime.now().plusDays(3), true, true)
        ));

        mvc.perform(post("/api/latest/user-info/coach")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {
                            "coach": null
                        }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user_with_coach")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.userRoles", hasSize(1)))
            .andExpect(jsonPath("$.userRoles", hasItems("USER")))
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.coach", nullValue()))
        ;

        final var sessions = scheduledSessionRepository.findAll();

        assertEquals(2, sessions.size());
    }


    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void pickCoachTest() throws Exception {
        mvc.perform(post("/api/latest/user-info/coach")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                            "coach": "coach2"
                        }
                    """)
                )
                .andExpect(status().isOk());

        var receivedMessage = greenMail.getReceivedMessages()[0];
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getFrom())).isEqualTo("TopLeaderPlatform@topleader.io");
        Assertions.assertThat(GreenMailUtil.getAddressList(receivedMessage.getAllRecipients())).isEqualTo("coach2");
        Assertions.assertThat(receivedMessage.getSubject()).isEqualTo("Byli jste vybráni jako kouč na platformě TopLeader!");
    }


    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void getUpcomingSessionsTest() throws Exception {

        var now = LocalDateTime.now().withNano(0);

        var dateTime1 = now.plusHours(3);
        var dateTime2 = now.plusDays(3);

        var id1 = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", dateTime1, false, false, ScheduledSession.Status.UPCOMING)
        ).getId();
        var id2 = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", dateTime2, false, false, ScheduledSession.Status.UPCOMING)
        ).getId();

        // Sorted by time descending (most recent first)
        mvc.perform(get("/api/latest/user-info/upcoming-sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(id2))
            .andExpect(jsonPath("$[0].coach").value("coach"))
            .andExpect(jsonPath("$[0].firstName").value("Mitch"))
            .andExpect(jsonPath("$[0].lastName").value("Cleverman"))
            .andExpect(jsonPath("$[0].status").value("UPCOMING"))
            .andExpect(jsonPath("$[1].id").value(id1))
            .andExpect(jsonPath("$[1].status").value("UPCOMING"));
    }

    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void getAllSessionsTest() throws Exception {

        // Clear existing sessions for this user
        scheduledSessionRepository.findAllByUsername("user_with_coach")
            .forEach(scheduledSessionRepository::delete);

        var now = LocalDateTime.now().withNano(0);

        var pastDateTime = now.minusDays(5);
        var futureDateTime = now.plusDays(3);

        // Past completed session
        var pastId = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", pastDateTime, true, false, ScheduledSession.Status.COMPLETED)
        ).getId();

        // Future upcoming session
        var futureId = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", futureDateTime, false, false, ScheduledSession.Status.UPCOMING)
        ).getId();

        // NO_SHOW_CLIENT session
        var noShowId = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", now.minusDays(3), true, false, ScheduledSession.Status.NO_SHOW_CLIENT)
        ).getId();

        mvc.perform(get("/api/latest/user-info/sessions"))
            .andExpect(status().isOk())
            .andExpect(content().json("""
                {
                  "summary": {
                    "allocatedUnits": 10,
                    "upcomingSessions": 1,
                    "pendingSessions": 0,
                    "completedSessions": 1,
                    "noShowClientSessions": 1,
                    "consumedUnits": 2,
                    "remainingUnits": 7
                  },
                  "sessions": [
                    { "id": %d, "status": "UPCOMING" },
                    { "id": %d, "status": "NO_SHOW_CLIENT" },
                    { "id": %d, "status": "COMPLETED" }
                  ]
                }
                """.formatted(futureId, noShowId, pastId)));
    }

    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void deleteUpcomingSessionsTest() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var dateTime1 = now.plusDays(2);
        final var dateTime2 = now.plusDays(3);


        final var id1 = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", dateTime1, false, false)
        ).getId();
        final var id2 = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", dateTime2, false, false)
        ).getId();

        mvc.perform(delete("/api/latest/user-info/upcoming-sessions/" + id1))
            .andExpect(status().isOk())
            ;

        var cancelledSession = scheduledSessionRepository.findById(id1);
        assertTrue(cancelledSession.isPresent());
        assertEquals(ScheduledSession.Status.CANCELED_BY_CLIENT, cancelledSession.get().getStatus());
        assertTrue(scheduledSessionRepository.findById(id2).isPresent());
    }

    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void deleteUpcomingPrivateSessionsTest() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var dateTime1 = now.plusDays(2);
        final var dateTime2 = now.plusDays(3);


        final var id1 = scheduledSessionRepository.save(
            createSession("user_with_coach", null, dateTime1, true, true)
        ).getId();
        final var id2 = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", dateTime2, false, false)
        ).getId();

        mvc.perform(delete("/api/latest/user-info/upcoming-sessions/" + id1))
            .andExpect(status().isOk())
            ;

        var cancelledSession = scheduledSessionRepository.findById(id1);
        assertTrue(cancelledSession.isPresent());
        assertEquals(ScheduledSession.Status.CANCELED_BY_CLIENT, cancelledSession.get().getStatus());
        assertTrue(scheduledSessionRepository.findById(id2).isPresent());
    }

    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void cancelSessionWithin24HoursFails() throws Exception {

        final var now = LocalDateTime.now().withNano(0);
        final var dateTimeWithin24h = now.plusHours(12);

        final var id = scheduledSessionRepository.save(
            createSession("user_with_coach", "coach", dateTimeWithin24h, false, false)
        ).getId();

        mvc.perform(delete("/api/latest/user-info/upcoming-sessions/" + id))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$[0].errorCode").value("session.cancel.too.late"));

        // Session should still exist with original status
        var session = scheduledSessionRepository.findById(id);
        assertTrue(session.isPresent());
        assertNotEquals(ScheduledSession.Status.CANCELED_BY_CLIENT, session.get().getStatus());
    }

    @Test
    @WithMockUser(username = "user_with_coach")
    void schedulePrivateSessionTest() throws Exception {

        final var scheduleTime = LocalDateTime.of(
            LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY)),
            LocalTime.of(9, 0)
        );

        mvc.perform(post("/api/latest/user-info/private-session")
                .contentType(MediaType.APPLICATION_JSON)
                .content(String.format("""
                    {
                        "time": "%s"
                    }
                    """, scheduleTime))
            )
            .andExpect(status().isOk())
            .andExpect(content().json(
                String.format(
                """
                    {
                      "coach": null,
                      "firstName": null,
                      "lastName": null,
                      "time": "%s",
                      "isPrivate": true
                    }
                    """
                , scheduleTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))))
        ;

        final var sessions = scheduledSessionRepository.findAll();

        assertThat(sessions, hasSize(2));

        final var session = sessions.get(1);

        assertThat(session.getCoachUsername(), nullValue());
        assertThat(session.getUsername(), is("user_with_coach"));
        assertThat(session.getTime(), is(scheduleTime.atZone(ZoneId.of("UTC")).withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime()));

        var allocation = userAllocationRepository.findActiveByUsername("user_with_coach").get(0);
        assertThat(allocation.getConsumedUnits(), is(1));
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight_user-info.sql", "/user_insight/ai-prompt.sql"})
    void setUserValues() throws Exception {

        Mockito.doReturn("leadership-response").when(aiClient)
                .findLeaderShipStyle(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList());
        Mockito.doReturn("animal-response").when(aiClient)
                .findAnimalSpirit(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList());
        Mockito.doReturn("world-leader-response").when(aiClient)
                .findLeaderPersona(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        mvc.perform(post("/api/latest/user-info/values")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "data": ["patriotism"]
                     }
                                        """)
                )
                .andExpect(status().isOk());

        Assertions.assertThat(userInsightRepository.findAll())
                .extracting("leadershipStyleAnalysis", "animalSpiritGuide", "worldLeaderPersona")
                .containsExactly(new Tuple("leadership-response", "animal-response", "world-leader-response"));

    }


    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight_user-info.sql", "/user_insight/ai-prompt.sql"})
    void setUserStrength() throws Exception {

        Mockito.doReturn("leadership-response").when(aiClient)
                .findLeaderShipStyle(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList());
        Mockito.doReturn("animal-response").when(aiClient)
                .findAnimalSpirit(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList());
        Mockito.doReturn("world-leader-response").when(aiClient)
                .findLeaderPersona(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyList());

        mvc.perform(post("/api/latest/user-info/strengths")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                        "data": ["selfBeliever"]
                     }
                    """)
                )
                .andExpect(status().isOk());

        Assertions.assertThat(userInsightRepository.findAll())
                .extracting("leadershipStyleAnalysis", "animalSpiritGuide", "worldLeaderPersona")
                .containsExactly(new Tuple("leadership-response", "animal-response", "world-leader-response"));

    }
}