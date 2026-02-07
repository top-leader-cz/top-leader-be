package com.topleader.topleader.user.userinsight;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.common.ai.AiPrompt;
import com.topleader.topleader.common.ai.AiPromptService;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.common.util.common.JsonUtils;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserInsightControllerIT extends IntegrationTest {

    @Autowired
    ChatModel chatModel;

    @Autowired
    ChatClient chatClient;

    @Autowired
    AiClient aiClient;

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserInsightRepository userInsightRepository;

    @Autowired
    AiPromptService aiPromptService;

    @Autowired
    ArticleRepository articleRepository;


    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql"})
    void getInsight() throws Exception {
        mvc.perform(get("/api/latest/user-insight")).andDo(print()).andExpect(content().json("""
                
                      {
                  "animalSpirit": {
                    "text": "animal-response",
                    "isPending": false
                  },
                  "leaderShipStyle": {
                    "text": "leadership-response",
                    "isPending": false
                  },
                  "userArticles": {
                    "text": "[{\\"url\\":\\"https://hbr.org/2018/04/better-brainstorming\\",\\"perex\\":\\"perex\\",\\"title\\":\\"title\\",\\"author\\":\\"Scott Berinato\\",\\"source\\":\\"Harvard Business Review\\",\\"language\\":\\"en\\",\\"readTime\\":\\"6 min read\\",\\"imageData\\":\\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==\\",\\"application\\":\\"application\\",\\"imagePrompt\\":\\"prompt\\",\\"summaryText\\":\\"summary\\",\\"id\\":1,\\"date\\":\\"2025-08-25\\",\\"imageUrl\\":\\"gs://ai-images-top-leader/test_image.png\\"}]",
                    "isPending": false
                  },
                  "userPreviews": {
                    "text": "test-user-previews",
                    "isPending": false
                  },
                  "leadershipTip": {
                    "text": null,
                    "isPending": false
                  },
                  "suggestion": {
                    "text": "suggestion",
                    "isPending": false
                  },
                  "personalGrowthTip": {
                    "text": null,
                    "isPending": false
                  },
                  "leaderPersona": {
                    "text": "world-leader-persona",
                    "isPending": false
                  }
                }
                """)).andExpect(status().isOk());

    }

    @Test
    @Disabled("we disabled this feature")
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql", "/user_insight/ai-prompt.sql"})
    void generateTips() throws Exception {
        var leaderShipQuery = String.format(aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_TIP), List.of("solver", "ideamaker", "flexible", "responsible", "selfBeliever"), List.of("patriotism"), "English");
        Mockito.when(chatModel.call(leaderShipQuery)).thenReturn("leadershipTip-response");

        mvc.perform(get("/api/latest/user-insight/generate-tips")).andDo(print()).andExpect(status().isOk());

        Assertions.assertThat(userInsightRepository.findAll()).extracting(UserInsight::getLeadershipTip, UserInsight::getPersonalGrowthTip).containsExactly(new Tuple("leadershipTip-response", null));

    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql", "/user_insight/ai-prompt.sql"})
    void generateTipsNoStrengthsAndValues() throws Exception {
        userInfoRepository.deleteAll();
        var leaderShipQuery = String.format(aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_TIP), List.of("solver", "ideamaker", "flexible", "responsible", "selfBeliever"), List.of("patriotism"), "en");
        Mockito.when(chatModel.call(leaderShipQuery)).thenReturn("leadershipTip-response");

        mvc.perform(get("/api/latest/user-insight/generate-tips")).andExpect(status().isOk());


        Assertions.assertThat(userInsightRepository.findAll()).extracting(UserInsight::getLeadershipTip, UserInsight::getPersonalGrowthTip).containsExactly(new Tuple(null, null));

    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql"})
    void fetchArticle() throws Exception {
        mvc.perform(get("/api/latest/user-insight/article/1")).andExpect(status().isOk()).andDo(print()).andExpect(content().json("""
                            {
                              "id": 1,
                              "url": "https://hbr.org/2018/04/better-brainstorming",
                              "perex": "perex",
                              "title": "title",
                              "author": "Scott Berinato",
                              "source": "Harvard Business Review",
                              "language": "en",
                              "readTime": "6 min read",
                              "imageData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==",
                              "application": "application",
                              "imagePrompt": "prompt",
                              "summaryText": "summary",
                              "date": "2025-08-25",
                              "imageUrl": "gs://ai-images-top-leader/test_image.png"
                            }
                
                """));
    }

    @Test

    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/dashboard-data.sql"})
    void dashboard() throws Exception {
        mockServer.stubFor(WireMock.get(urlEqualTo("/hqdefault")).willReturn(aResponse().withStatus(200).withBody("ok")));
        mockServer.stubFor(WireMock.post(urlEqualTo("/image")).willReturn(aResponse().withStatus(200).withBody("{\"data\":[{\"url\":\"http://localhost:8060/test-image.png\"}]}")));

        Mockito.when(chatModel.call("video [query]")).thenReturn("""
                               [
                  {
                    "title": "Test Preview",
                    "url": "https://youtube.com/watch?v=test",
                    "thumbnail": "http://localhost:8060/hqdefault"
                  }
                ]
                """);


        var articlesJson = """
                  [
                  {
                    "url": "https://example.com/articles/leadership-principles",
                    "perex": "Discover the fundamental principles that define successful leadership in modern organizations and how to apply them effectively.",
                    "title": "5 Essential Leadership Principles for Modern Teams",
                    "author": "Jane Smith",
                    "source": "Leadership Weekly",
                    "language": "en",
                    "readTime": "8 min",
                    "imageData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
                    "application": "Learn to implement these principles through daily team interactions, strategic decision-making, and fostering a culture of trust and accountability.",
                    "imagePrompt": "A diverse team collaborating around a modern conference table with natural lighting",
                    "summaryText": "This article explores five core leadership principles including emotional intelligence, transparent communication, empowerment, adaptability, and continuous learning. It provides practical strategies for implementing each principle in day-to-day management.",
                    "id": 12345,
                    "imageUrl": "https://example.com/images/leadership-team.jpg",
                    "date": "2025-11-01"
                  }
                  ]
                """;

        // Mock AiClient methods directly using spy
        var mockArticles = JsonUtils.fromJson(articlesJson, new ParameterizedTypeReference<List<UserArticle>>() {});
        Mockito.doReturn("suggestion response").when(aiClient)
                .generateSuggestion(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyList(), ArgumentMatchers.anyList(), ArgumentMatchers.anyString());
        Mockito.doReturn(mockArticles).when(aiClient)
                .generateUserArticles(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyString());


        mvc.perform(post("/api/latest/user-insight/dashboard").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "query": "query"
                }
                """)).andDo(print()).andExpect(status().isOk());


        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var userInsight = userInsightRepository.findByUsername("user").orElseThrow();

                    // Check suggestion first (should be synchronous)
                    assertThat(userInsight.isSuggestionPending()).isFalse();
                    assertThat(userInsight.getSuggestion()).isEqualTo("suggestion response");

                    // Check articles
                    var articles = articleRepository.findByUsername("user");
                    assertThat(articles).hasSize(1);
                    assertThat(articles.get(0).getContent().getTitle()).isEqualTo("5 Essential Leadership Principles for Modern Teams");
                    assertThat(articles.get(0).getContent().getAuthor()).isEqualTo("Jane Smith");

                    // Check userPreviews (async operation)
                    assertThat(userInsight.getUserPreviews()).isNotNull();
                    TestUtils.assertJsonEquals(userInsight.getUserPreviews(), """
                                                        [ {
                              "title" : "Test Preview",
                              "url" : "https://youtube.com/watch?v=test",
                              "thumbnail" : "http://localhost:8060/hqdefault"
                            } ]
                            """);
                    assertThat(userInsight.isActionGoalsPending()).isFalse();
                });
    }


}