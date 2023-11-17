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
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
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

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    void getEmptyDetailTest() throws Exception {

        mvc.perform(get("/api/latest/user-info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username", is("user")))
            .andExpect(jsonPath("$.timeZone", is("UTC")))
            .andExpect(jsonPath("$.userRoles", hasSize(1)))
            .andExpect(jsonPath("$.userRoles", hasItems("USER")))
            .andExpect(jsonPath("$.strengths", hasSize(0)))
            .andExpect(jsonPath("$.values", hasSize(0)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.notes", nullValue()))
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
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(LocalDateTime.now().plusHours(3)),
            new ScheduledSession()
                .setPaid(false)
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

        scheduledSessionRepository.saveAll(List.of(
            new ScheduledSession()
                .setPaid(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(dateTime1),
            new ScheduledSession()
                .setPaid(false)
                .setUsername("user_with_coach")
                .setCoachUsername("coach")
                .setTime(dateTime2)
        ));

        mvc.perform(get("/api/latest/user-info/upcoming-sessions"))
            .andExpect(status().isOk())
            .andExpect(content().json(String.format("""
                [
                  {
                    "coach": "coach",
                    "firstName": "Mitch",
                    "lastName": "Cleverman",
                    "time": "%s"
                  },
                  {
                    "coach": "coach",
                    "firstName": "Mitch",
                    "lastName": "Cleverman",
                    "time": "%s"
                  }
                ]
                """, dateTime1, dateTime2)))
        ;
    }
}