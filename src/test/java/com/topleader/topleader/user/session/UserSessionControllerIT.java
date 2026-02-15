/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.topleader.topleader.user.badge.Badge;
import com.topleader.topleader.user.badge.BadgeRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


/**
 * @author Daniel Slavik
 */
@Sql(scripts = {"/sql/user_info/session/user-session.sql", "/sql/user_info/session/ai-prompt.sql"})
class UserSessionControllerIT extends IntegrationTest {

    @Autowired
    private DataHistoryRepository dataHistoryRepository;

    @Autowired
    private UserActionStepRepository userActionStepRepository;

    @Autowired
    BadgeRepository badgeRepository;



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

        final var historyData = dataHistoryRepository.findByUsernameAndType("user", DataHistory.Type.USER_SESSION.name());

        assertThat(historyData, hasSize(1));
        assertThat(historyData.get(0).getData(), instanceOf(UserSessionStoredData.class));

        final var sessionData = (UserSessionStoredData) historyData.get(0).getData();

        assertThat(sessionData.getReflection(), nullValue());
        assertThat(sessionData.getMotivation(), is("you can do it!"));
        assertThat(sessionData.getLongTermGoal(), is("win"));
        assertThat(sessionData.getAreaOfDevelopment(), hasSize(2));
        assertThat(sessionData.getAreaOfDevelopment(), hasItems("a1", "a2"));

        var now = LocalDateTime.now();
        var badges = badgeRepository.getUserBadges("user", now.getYear());
        Assertions.assertThat(badges.stream()
                .filter(b -> b.getAchievementType() == Badge.AchievementType.COMPLETE_SESSION)
                .filter(b -> b.getMonth() == now.getMonth())
                .findFirst()).isNotEmpty();

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

        final var historyData = dataHistoryRepository.findByUsernameAndType("user2", DataHistory.Type.USER_SESSION.name());

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

    @Test
    @WithMockUser("user2")
    void generateLongTermGoal() throws Exception {
        stubAiResponse("generate three long-term goals", "[\"generated-long-term-goal-a.\",\"generated-long-term-goal-b.\"]");
        mvc.perform(post("/api/latest/user-sessions/generate-long-term-goal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "areaOfDevelopment": "area-of-development"
                                }
                                """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", hasItems("generated-long-term-goal-a.", "generated-long-term-goal-b.")))
        ;
    }

    @Test
    @WithMockUser("user2")
    void generateActionsSteps() throws Exception {
        stubAiResponse("Based on the user's top 5 talents", "[\"generated-actions-steps-a\",\"generated-actions-steps-b\"]");
        mvc.perform(post("/api/latest/user-sessions/generate-action-steps")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "areaOfDevelopment": "area-of-development",
                                    "longTermGoal": "generated-long-term-goal"
                                }
                                """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$", hasItems("generated-actions-steps-a", "generated-actions-steps-b")))
        ;
    }

    @Test
    @WithUserDetails("user2")
    void generateRecommendedGrowth() throws Exception {
        var expectedJson = """
                [
                  {"area":"Advanced Programming Skills","recommendation":"Enhance your programming expertise by learning advanced languages and frameworks that are in high demand, which can lead to higher-paying roles."},
                  {"area":"Business Acumen in Tech","recommendation":"Develop a deeper understanding of the business side of technology, including how tech solutions can drive revenue, to align your technical skills with business growth strategies."},
                  {"area":"Project Management","recommendation":"Learn project management methodologies like Agile or Scrum to take on leadership roles in projects, which often come with increased salary potential."}
                ]
                """;

        stubAiResponse("test query", expectedJson);

        var result = mvc.perform(get("/api/latest/user-sessions/generate-recommended-growth"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        TestUtils.assertJsonEquals(result, TestUtils.readFileAsString("session/json/recommended-growth-result.json"));
    }

    @Test
    @WithUserDetails("user2")
    @Sql(scripts = {"/sql/user_info/session/user-session.sql", "/sql/user_info/session/ai-prompt.sql", "/sql/user/feedback/feedback.sql"})
   void setFeedback() throws Exception {
        mvc.perform(post("/api/latest/user-sessions/feedback")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                   "answers": {"a1" :  1, "a2" :  2, "a3":  3},
                                   "feedback": "long text"
                                }
                                """)
                )
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().json("""
                        {
                          "username": "user2",
                          "sessionId": 2,
                          "answers": {
                            "a1": 1,
                            "a2": 2,
                            "a3": 3
                          },
                          "feedback": "long text"
                        }

                        """));
    }

    private Badge badge(Badge.AchievementType type) {
        var now = LocalDateTime.now();
        return new Badge().setUsername("user").setAchievementType(type).setMonth(now.getMonth()).setYear(now.getYear());
    }


}
