/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/user_info/session/user-session.sql")
class UserSessionControllerIT extends IntegrationTest {

    @Autowired
    private DataHistoryRepository dataHistoryRepository;

    @Autowired
    private UserActionStepRepository userActionStepRepository;

    @Test
    @WithMockUser("user2")
    void getUserSessionData() throws Exception {
        mvc.perform(get("/api/latest/user-sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasItems("a1", "a2")))
            .andExpect(jsonPath("$.longTermGoal", is("some cool goal")))
            .andExpect(jsonPath("$.motivation", is("I wanna be cool")))
            .andExpect(jsonPath("$.lastReflection", is("I am cool")))
            .andExpect(jsonPath("$.actionSteps", hasSize(2)))
            .andExpect(jsonPath("$.actionSteps[0].label", is("action 1")))
            .andExpect(jsonPath("$.actionSteps[0].date", is("2023-08-14")))
            .andExpect(jsonPath("$.actionSteps[0].checked", is(true)))
            .andExpect(jsonPath("$.actionSteps[1].label", is("action 2")))
            .andExpect(jsonPath("$.actionSteps[1].date", is("2023-08-15")))
            .andExpect(jsonPath("$.actionSteps[1].checked", is(false)))
        ;
    }

    @Test
    @WithMockUser()
    void getEmptyUserSessionData() throws Exception {
        mvc.perform(get("/api/latest/user-sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(0)))
            .andExpect(jsonPath("$.longTermGoal", nullValue()))
            .andExpect(jsonPath("$.motivation", nullValue()))
            .andExpect(jsonPath("$.actionSteps", hasSize(0)))
        ;
    }

    @Test
    @WithMockUser
    void setEmptyUserSessionData() throws Exception {
        mvc.perform(post("/api/latest/user-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                       "areaOfDevelopment": ["a1", "a2"],
                       "longTermGoal": "win",
                       "motivation": "you can do it!",
                       "actionSteps": [{"label": "do not lose", "date": "2023-08-15"}]
                    }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasItems("a1", "a2")))
            .andExpect(jsonPath("$.longTermGoal", is("win")))
            .andExpect(jsonPath("$.motivation", is("you can do it!")))
            .andExpect(jsonPath("$.lastReflection", nullValue()))
            .andExpect(jsonPath("$.actionSteps", hasSize(1)))
            .andExpect(jsonPath("$.actionSteps[0].label", is("do not lose")))
            .andExpect(jsonPath("$.actionSteps[0].date", is("2023-08-15")))
            .andExpect(jsonPath("$.actionSteps[0].checked", is(false)))
        ;

        final var historyData = dataHistoryRepository.findAllByUsernameAndType("user", DataHistory.Type.USER_SESSION);

        assertThat(historyData, hasSize(1));
        assertThat(historyData.get(0).getData(), instanceOf(UserSessionStoredData.class));

        final var sessionData = (UserSessionStoredData) historyData.get(0).getData();

        assertThat(sessionData.getReflection(), nullValue());
        assertThat(sessionData.getMotivation(), is("you can do it!"));
        assertThat(sessionData.getLongTermGoal(), is("win"));
        assertThat(sessionData.getAreaOfDevelopment(), hasSize(2));
        assertThat(sessionData.getAreaOfDevelopment(), hasItems("a1", "a2"));

    }

    @Test
    @WithMockUser("user2")
    void setUserSessionData() throws Exception {
        mvc.perform(post("/api/latest/user-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                       "areaOfDevelopment": ["a1", "a2"],
                       "longTermGoal": "win",
                       "motivation": "you can do it!",
                       "actionSteps": [{"label": "do not lose", "date": "2023-08-15"}]
                    }
                    """)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.areaOfDevelopment", hasSize(2)))
            .andExpect(jsonPath("$.areaOfDevelopment", hasItems("a1", "a2")))
            .andExpect(jsonPath("$.longTermGoal", is("win")))
            .andExpect(jsonPath("$.motivation", is("you can do it!")))
            .andExpect(jsonPath("$.actionSteps", hasSize(1)))
            .andExpect(jsonPath("$.actionSteps[0].label", is("do not lose")))
            .andExpect(jsonPath("$.actionSteps[0].date", is("2023-08-15")))
            .andExpect(jsonPath("$.actionSteps[0].checked", is(false)))
        ;

        final var historyData = dataHistoryRepository.findAllByUsernameAndType("user2", DataHistory.Type.USER_SESSION);

        assertThat(historyData, hasSize(1));
        assertThat(historyData.get(0).getData(), instanceOf(UserSessionStoredData.class));

        final var sessionData = (UserSessionStoredData) historyData.get(0).getData();

        assertThat(sessionData.getReflection(), nullValue());
        assertThat(sessionData.getMotivation(), is("you can do it!"));
        assertThat(sessionData.getLongTermGoal(), is("win"));
        assertThat(sessionData.getAreaOfDevelopment(), hasSize(2));
        assertThat(sessionData.getAreaOfDevelopment(), hasItems("a1", "a2"));

        final var userActionSteps = userActionStepRepository.findAllByUsername("user2");
        assertThat(userActionSteps, hasSize(1));
        assertThat(userActionSteps.get(0).getChecked(), is(false));
        assertThat(userActionSteps.get(0).getLabel(), is("do not lose"));
        assertThat(userActionSteps.get(0).getDate(), is(LocalDate.parse("2023-08-15")));
    }
}
