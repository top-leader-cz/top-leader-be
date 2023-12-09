/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = "/sql/user_info/session/user-session.sql")
class UserSessionReflectionControllerIT extends IntegrationTest {

    @Autowired
    private DataHistoryRepository dataHistoryRepository;

    @Autowired
    private UserActionStepRepository userActionStepRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Test
    @WithMockUser("user2")
    void setUserSessionReflectionData() throws Exception {
        mvc.perform(post("/api/latest/user-sessions-reflection")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                       "reflection": "you can do it!",
                       "checked": [2],
                       "newActionSteps": [{"label": "do not lose", "date": "2023-08-15"}]
                    }
                    """)
            )
            .andExpect(status().isOk())

        ;

        final var historyData = dataHistoryRepository.findAllByUsernameAndType("user2", DataHistory.Type.USER_SESSION);

        assertThat(historyData, hasSize(1));
        assertThat(historyData.get(0).getData(), instanceOf(UserSessionStoredData.class));

        final var sessionData = (UserSessionStoredData) historyData.get(0).getData();

        assertThat(sessionData.getMotivation(), nullValue());
        assertThat(sessionData.getReflection(), is("you can do it!"));
        assertThat(sessionData.getLongTermGoal(), is("some cool goal"));
        assertThat(sessionData.getAreaOfDevelopment(), hasSize(2));
        assertThat(sessionData.getAreaOfDevelopment(), hasItems("a1", "a2"));

        final var userActionSteps = userActionStepRepository.findAllByUsername("user2");
        assertThat(userActionSteps, hasSize(2));
        final var doNotLoseStep = userActionSteps.stream().filter(s -> "do not lose".equals(s.getLabel())).findAny().orElseThrow();
        assertThat(doNotLoseStep.getChecked(), is(false));
        assertThat(doNotLoseStep.getLabel(), is("do not lose"));
        assertThat(doNotLoseStep.getDate(), is(LocalDate.parse("2023-08-15")));
        final var action2Step = userActionSteps.stream().filter(s -> "action 2".equals(s.getLabel())).findAny().orElseThrow();
        assertThat(action2Step.getChecked(), is(true));
        assertThat(action2Step.getLabel(), is("action 2"));
        assertThat(action2Step.getDate(), is(LocalDate.parse("2023-08-15")));

        assertThat(userInfoRepository.findById("user2").orElseThrow().getLastReflection(), is("you can do it!"));
    }

}
