package com.topleader.topleader.user.userinfo;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.credit.history.CreditHistory;
import com.topleader.topleader.credit.history.CreditHistoryRepository;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.StrengthStoredData;
import com.topleader.topleader.history.data.ValuesStoredData;
import com.topleader.topleader.scheduled_session.ScheduledSession;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
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

import com.topleader.topleader.user.userinsight.UserInsightRepository;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static com.topleader.topleader.ai.AiClient.ANIMAL_SPIRIT_QUERY;
import static com.topleader.topleader.ai.AiClient.LEADERSHIP_STYLE_QUERY;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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
    private CreditHistoryRepository creditHistoryRepository;

    @Autowired
    ChatClient chatClient;

    @Autowired
    UserInsightRepository userInsightRepository;

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
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
            .andExpect(jsonPath("$.locale",is("en")))
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
            .andExpect(jsonPath("$.notes", is("cool note")))
            .andExpect(jsonPath("$.locale", is("en")))
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
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
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
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
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
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", is("some cool notes")))
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
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
        ;

        final var userInfoData = userInfoRepository.findById("user").orElseThrow();

        assertIterableEquals(List.of("v1", "v2"), userInfoData.getStrengths());

        final var data = dataHistoryRepository.findAllByUsernameAndType("user", DataHistory.Type.STRENGTHS);

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
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
        ;

        final var userInfoData = userInfoRepository.findById("user").orElseThrow();

        assertIterableEquals(List.of("v1", "v2"), userInfoData.getValues());

        final var data = dataHistoryRepository.findAllByUsernameAndType("user", DataHistory.Type.VALUES);

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
            new ScheduledSession()
                .setPaid(false)
                .setPrivate(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(LocalDateTime.now().plusHours(3)),
            new ScheduledSession()
                .setPaid(false)
                .setPrivate(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(LocalDateTime.now().plusDays(3))
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
            .andExpect(jsonPath("$.notes", nullValue()))
            .andExpect(jsonPath("$.coach", nullValue()))
        ;

        final var sessions = scheduledSessionRepository.findAll();

        assertEquals(1, sessions.size());

        final var user = userRepository.findById("user_with_coach").orElseThrow();

        assertEquals(180, user.getScheduledCredit());
        assertEquals(400, user.getCredit());

        final var history = creditHistoryRepository.findAll();

        assertEquals(2, history.size());

        assertEquals(CreditHistory.Type.CANCELED, history.get(0).getType());
        assertEquals(CreditHistory.Type.CANCELED, history.get(1).getType());
        assertEquals(110, history.get(0).getCredit());
        assertEquals(110, history.get(1).getCredit());
    }

    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void getUpcomingSessionsTest() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var dateTime1 = now.plusHours(3);
        final var dateTime2 = now.plusDays(3);


        final var id1 = scheduledSessionRepository.save(
            new ScheduledSession()
                .setPaid(false)
                .setPrivate(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(dateTime1)
        ).getId();
        final var id2 = scheduledSessionRepository.save(
            new ScheduledSession()
                .setPaid(false)
                .setPrivate(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(dateTime2)
        ).getId();

        mvc.perform(get("/api/latest/user-info/upcoming-sessions"))
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                [
                  {
                    "id": %s,
                    "coach": "coach",
                    "firstName": "Mitch",
                    "lastName": "Cleverman",
                    "time": "%s"
                  },
                  {
                    "id": %s,
                    "coach": "coach",
                    "firstName": "Mitch",
                    "lastName": "Cleverman",
                    "time": "%s"
                  }
                ]
                """, id1, dateTime1, id2, dateTime2)))
        ;
    }

    @Test
    @WithMockUser(username = "user_with_coach", authorities = "USER")
    void deleteUpcomingSessionsTest() throws Exception {

        final var now = LocalDateTime.now().withNano(0);

        final var dateTime1 = now.plusHours(3);
        final var dateTime2 = now.plusDays(3);


        final var id1 = scheduledSessionRepository.save(
            new ScheduledSession()
                .setPaid(false)
                .setPrivate(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(dateTime1)
        ).getId();
        final var id2 = scheduledSessionRepository.save(
            new ScheduledSession()
                .setPaid(false)
                .setPrivate(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(dateTime2)
        ).getId();

        mvc.perform(delete("/api/latest/user-info/upcoming-sessions/" + id1))
            .andExpect(status().isOk())
            ;

        assertTrue(scheduledSessionRepository.findById(id1).isEmpty());
        assertTrue(scheduledSessionRepository.findById(id2).isPresent());
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

        final var user = userRepository.findById("user_with_coach").orElseThrow();

        assertThat(user.getScheduledCredit(), is(400));
        assertThat(user.getCredit(), is(400));

        final var creditHistory = creditHistoryRepository.findAll();

        assertThat(creditHistory, hasSize(0));
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight_user-info.sql"})
    void setUserInsight() throws Exception {

        var leaderShipQuery  = String.format(LEADERSHIP_STYLE_QUERY, List.of("solver","ideamaker","flexible","responsible","selfBeliever"), List.of("patriotism"), "English");
        Mockito.when(chatClient.call(leaderShipQuery)).thenReturn("leadership-response");

        var animalQuery  = String.format(ANIMAL_SPIRIT_QUERY, List.of("solver","ideamaker","flexible","responsible","selfBeliever"), List.of("patriotism"), "English");
        Mockito.when(chatClient.call(animalQuery)).thenReturn("animal-response");

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
                .extracting("leadershipStyleAnalysis", "animalSpiritGuide")
                .containsExactly(new Tuple("leadership-response", "animal-response"));

    }
}