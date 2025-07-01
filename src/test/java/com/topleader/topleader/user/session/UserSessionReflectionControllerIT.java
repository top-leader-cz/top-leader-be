/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.ai.AiPromptService;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.badge.Badge;
import com.topleader.topleader.user.badge.BadgeRepository;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.topleader.topleader.user.userinsight.UserInsightRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = {"/sql/user_info/session/user-session.sql", "/user_session/action_goals.sql"})
class UserSessionReflectionControllerIT extends IntegrationTest {

    @Autowired
    private DataHistoryRepository dataHistoryRepository;

    @Autowired
    private UserActionStepRepository userActionStepRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    UserInsightRepository userInsightRepository;

    @Autowired
    BadgeRepository badgeRepository;

    @Autowired
    ChatModel chatClient;


    @Test
    @WithMockUser("user2")
    void setUserSessionReflectionData() throws Exception {
        var actionGoal = "test {0} test {1} test {2} test {4} test {5} test {6}";
        Mockito.when(chatClient.call(actionGoal)).thenReturn("action-goal-response");

        mvc.perform(post("/api/latest/user-sessions-reflection")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "areaOfDevelopment": ["b1", "b2"],
                                   "longTermGoal": "newGoal",
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
        assertThat(sessionData.getLongTermGoal(), is("newGoal"));
        assertThat(sessionData.getAreaOfDevelopment(), hasSize(2));
        assertThat(sessionData.getAreaOfDevelopment(), hasItems("b1", "b2"));

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

        Thread.sleep(100);
        Assertions.assertThat(userInsightRepository.findAll())
                .extracting("personalGrowthTip")
                .contains("action-goal-response");

        Assertions.assertThat(badgeRepository.findOne(Example.of(badge(Badge.AchievementType.COMPLETED_SHORT_GOAL)))).isNotEmpty();
        Assertions.assertThat(badgeRepository.findOne(Example.of(badge(Badge.AchievementType.COMPLETE_SESSION)))).isNotEmpty();
    }

    private Badge badge(Badge.AchievementType type) {
        var now = LocalDateTime.now();
        return new Badge().setBadgeId(new Badge.BadgeId("user2", type, now.getMonth(), now.getYear()));
    }

}
