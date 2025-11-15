/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.badge.Badge;
import com.topleader.topleader.user.badge.BadgeRepository;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.topleader.topleader.user.userinsight.UserInsightRepository;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.util.common.JsonUtils;
import org.assertj.core.api.Assertions;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Example;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.time.Duration;
import java.util.List;

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
    ChatModel chatModel;

    @Autowired
    ChatClient chatClient;

    @Autowired
    ArticleRepository articleRepository;


    @Test
    @WithMockUser("user2")
    void setUserSessionReflectionData() throws Exception {
        // Mock AI client response for user articles generation
        var expectedArticlesJson = """
                [
                    {
                        "title": "Effective Leadership Communication",
                        "author": "John Doe",
                        "source": "Leadership Journal",
                        "url": "http://localhost:8060/article1",
                        "readTime": "5 min",
                        "language": "en",
                        "perex": "Learn how to communicate effectively as a leader",
                        "summaryText": "This article covers key communication strategies for leaders",
                        "application": "Apply these techniques in daily team meetings",
                        "imagePrompt": "leadership communication meeting",
                        "date": "2023-10-01"
                    },
                    {
                        "title": "Building Team Motivation",
                        "author": "Jane Smith",
                        "source": "Management Today",
                        "url": "http://localhost:8060/article2",
                        "readTime": "3 min",
                        "language": "en",
                        "perex": "Discover ways to keep your team motivated",
                        "summaryText": "Methods for maintaining high team morale and productivity",
                        "application": "Implement motivational strategies in your team",
                        "imagePrompt": "team motivation office",
                        "date": "2023-10-02"
                    }
                ]
                """;

        Mockito.when(chatModel.call(Mockito.anyString()))
                .thenReturn("action-goal-response")
                .thenReturn(expectedArticlesJson);

        Mockito.when(chatClient.prompt(ArgumentMatchers.any(Prompt.class)).call().entity(new ParameterizedTypeReference<List<UserArticle>>() {}))
                .thenReturn(JsonUtils.fromJson(expectedArticlesJson, new ParameterizedTypeReference<>() {}));

        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/article1"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody("<html><body>Valid article content</body></html>")));

        mockServer.stubFor(WireMock.get(WireMock.urlEqualTo("/article2"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "text/html")
                        .withBody("<html><body>Another valid article</body></html>")));

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

        Assertions.assertThat(historyData).hasSize(1);
        Assertions.assertThat(historyData.get(0).getData()).isInstanceOf(UserSessionStoredData.class);

        final var sessionData = (UserSessionStoredData) historyData.get(0).getData();

        Assertions.assertThat(sessionData.getMotivation()).isNull();
        Assertions.assertThat(sessionData.getReflection()).isEqualTo("you can do it!");
        Assertions.assertThat(sessionData.getLongTermGoal()).isEqualTo("newGoal");
        Assertions.assertThat(sessionData.getAreaOfDevelopment()).hasSize(2);
        Assertions.assertThat(sessionData.getAreaOfDevelopment()).contains("b1", "b2");

        final var userActionSteps = userActionStepRepository.findAllByUsername("user2");
        Assertions.assertThat(userActionSteps).hasSize(2);
        final var doNotLoseStep = userActionSteps.stream().filter(s -> "do not lose".equals(s.getLabel())).findAny().orElseThrow();
        Assertions.assertThat(doNotLoseStep.getChecked()).isEqualTo(false);
        Assertions.assertThat(doNotLoseStep.getLabel()).isEqualTo("do not lose");
        Assertions.assertThat(doNotLoseStep.getDate()).isEqualTo(LocalDate.parse("2023-08-15"));
        final var action2Step = userActionSteps.stream().filter(s -> "action 2".equals(s.getLabel())).findAny().orElseThrow();
        Assertions.assertThat(action2Step.getChecked()).isEqualTo(true);
        Assertions.assertThat(action2Step.getLabel()).isEqualTo("action 2");
        Assertions.assertThat(action2Step.getDate()).isEqualTo(LocalDate.parse("2023-08-15"));

        Assertions.assertThat(userInfoRepository.findById("user2").orElseThrow().getLastReflection()).isEqualTo("you can do it!");


        Awaitility.await()
                .atMost(Duration.ofSeconds(3))
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    Assertions.assertThat(userInsightRepository.findAll())
                            .extracting("personalGrowthTip")
                            .contains("action-goal-response");
                });


        Awaitility.await()
                .atMost(Duration.ofSeconds(5))
                .pollInterval(Duration.ofMillis(500))
                .untilAsserted(() -> {
                    var articles = articleRepository.findByUsername("user2");
                    Assertions.assertThat(articles).hasSize(2);
                });

        var articles = articleRepository.findByUsername("user2");
        Assertions.assertThat(articles)
                .extracting("content.title")
                .contains("Effective Leadership Communication", "Building Team Motivation");
        Assertions.assertThat(articles)
                .extracting("content.author") 
                .contains("John Doe", "Jane Smith");
        Assertions.assertThat(articles)
                .extracting("content.imagePrompt")
                .contains("leadership communication meeting", "team motivation office");
        Assertions.assertThat(articles)
                .extracting("content.date")
                .contains( "2023-10-01",  "2023-10-02");

        Assertions.assertThat(badgeRepository.findOne(Example.of(badge(Badge.AchievementType.COMPLETED_SHORT_GOAL)))).isNotEmpty();
        Assertions.assertThat(badgeRepository.findOne(Example.of(badge(Badge.AchievementType.COMPLETE_SESSION)))).isNotEmpty();
    }

    private Badge badge(Badge.AchievementType type) {
        var now = LocalDateTime.now();
        return new Badge().setBadgeId(new Badge.BadgeId("user2", type, now.getMonth(), now.getYear()));
    }

}
