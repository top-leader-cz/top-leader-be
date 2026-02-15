package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.TestUtils;
import com.topleader.topleader.common.ai.McpToolsConfig;
import com.topleader.topleader.user.userinfo.UserInfoRepository;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.common.util.common.JsonUtils;
import okhttp3.mockwebserver.MockResponse;
import org.assertj.core.api.Assertions;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserInsightControllerIT extends IntegrationTest {

    @Autowired
    UserInfoRepository userInfoRepository;

    @Autowired
    UserInsightRepository userInsightRepository;

    @Autowired
    ArticleRepository articleRepository;

    @Autowired
    Function<McpToolsConfig.UserProfileRequest, McpToolsConfig.UserProfileResponse> getUserProfile;

    @Autowired
    Function<McpToolsConfig.CoachSearchRequest, List<McpToolsConfig.CoachResponse>> searchCoaches;

    @Autowired
    Function<McpToolsConfig.CoachByNameRequest, java.util.Optional<McpToolsConfig.CoachResponse>> getCoachByName;

    @Autowired
    @Qualifier("searchArticles")
    Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> mockSearchArticles;

    @Autowired
    @Qualifier("searchVideos")
    Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> mockSearchVideos;


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
                    "text": "[{\\"url\\":\\"https://hbr.org/2018/04/better-brainstorming\\",\\"perex\\":\\"perex\\",\\"title\\":\\"title\\",\\"author\\":\\"Scott Berinato\\",\\"source\\":\\"Harvard Business Review\\",\\"language\\":\\"en\\",\\"readTime\\":\\"6 min read\\",\\"imageData\\":\\"data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg==\\",\\"application\\":\\"application\\",\\"imagePrompt\\":\\"prompt\\",\\"summaryText\\":\\"summary\\",\\"id\\":1,\\"date\\":\\"2025-08-25\\",\\"imageUrl\\":\\"gs://ai-images-top-leader/test_image.png\\",\\"keyTakeaways\\":null,\\"originalTitle\\":null,\\"relevanceScore\\":null,\\"sourceLanguage\\":null}]",
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
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/user-insight.sql", "/user_insight/ai-prompt.sql"})
    void generateTipsNoStrengthsAndValues() throws Exception {
        userInfoRepository.deleteAll();

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
        stubResponse("/hqdefault", () -> new MockResponse().setResponseCode(200).setBody("ok"));
        stubResponse("/image", () -> new MockResponse().setResponseCode(200).setBody("{\"data\":[{\"url\":\"http://localhost:8060/test-image.png\"}]}"));

        var previewsJson = """
                [
                  {
                    "title": "Test Preview",
                    "url": "https://youtube.com/watch?v=test",
                    "thumbnail": "http://localhost:8060/hqdefault"
                  }
                ]
                """;

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

        // Configure Tavily mocks to return search results
        Mockito.when(mockSearchVideos.apply(Mockito.any())).thenReturn(List.of(
                new McpToolsConfig.TavilySearchResult("Test Video", "https://youtube.com/watch?v=test", "A test video about leadership")
        ));
        Mockito.when(mockSearchArticles.apply(Mockito.any())).thenReturn(List.of(
                new McpToolsConfig.TavilySearchResult("Leadership Article", "https://example.com/articles/leadership-principles", "Article about leadership principles")
        ));

        // Stub AI responses
        stubAiResponse("Translate the following text to English", "english goals");
        stubAiResponse("video", previewsJson);
        stubAiResponse("suggestion", "suggestion response");
        stubAiResponse("article", articlesJson);

        mvc.perform(post("/api/latest/user-insight/dashboard?useMcp=false").contentType(MediaType.APPLICATION_JSON).content("""
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

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/dashboard-data.sql"})
    void dashboardWithMcp() throws Exception {
        stubResponse("/hqdefault", () -> new MockResponse().setResponseCode(200).setBody("ok"));
        stubResponse("/image", () -> new MockResponse().setResponseCode(200).setBody("{\"data\":[{\"url\":\"http://localhost:8060/test-image.png\"}]}"));

        var previewsJson = """
                [
                  {
                    "title": "Test Preview",
                    "url": "https://youtube.com/watch?v=test",
                    "thumbnail": "http://localhost:8060/hqdefault"
                  }
                ]
                """;

        var articlesJson = """
                [
                  {
                    "url": "https://example.com/articles/leadership-principles",
                    "perex": "Discover the fundamental principles.",
                    "title": "5 Essential Leadership Principles",
                    "author": "Jane Smith",
                    "source": "Leadership Weekly",
                    "language": "en",
                    "readTime": "8 min",
                    "imageData": "data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==",
                    "application": "Learn to implement these principles.",
                    "imagePrompt": "A diverse team collaborating",
                    "summaryText": "This article explores five core leadership principles.",
                    "id": 12345,
                    "imageUrl": "https://example.com/images/leadership-team.jpg",
                    "date": "2025-11-01"
                  }
                ]
                """;

        // Configure Tavily mocks to return search results
        Mockito.when(mockSearchVideos.apply(Mockito.any())).thenReturn(List.of(
                new McpToolsConfig.TavilySearchResult("Test Video", "https://youtube.com/watch?v=test", "A test video")
        ));
        Mockito.when(mockSearchArticles.apply(Mockito.any())).thenReturn(List.of(
                new McpToolsConfig.TavilySearchResult("Leadership Article", "https://example.com/articles/leadership-principles", "Article about leadership")
        ));

        // Stub AI responses
        stubAiResponse("Translate the following text to English", "english goals");
        stubAiResponse("video", previewsJson);
        stubAiResponse("article", articlesJson);

        // MCP tool call: first turn triggers getUserProfile, second turn returns content
        stubAiToolCall("TOOL INSTRUCTIONS", "getUserProfile", "{\"username\":\"user\"}");
        stubAiResponse("TOOL INSTRUCTIONS", "mcp suggestion response");

        mvc.perform(post("/api/latest/user-insight/dashboard?useMcp=true").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "query": "recommend me a coach"
                }
                """)).andDo(print()).andExpect(status().isOk());

        await().atMost(10, TimeUnit.SECONDS)
                .pollInterval(1, TimeUnit.SECONDS)
                .untilAsserted(() -> {
                    var userInsight = userInsightRepository.findByUsername("user").orElseThrow();

                    assertThat(userInsight.isSuggestionPending()).isFalse();
                    assertThat(userInsight.getSuggestion()).isEqualTo("mcp suggestion response");

                    var articles = articleRepository.findByUsername("user");
                    assertThat(articles).hasSize(1);

                    assertThat(userInsight.getUserPreviews()).isNotNull();
                    assertThat(userInsight.isActionGoalsPending()).isFalse();
                });
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/mcp-tools-test.sql"})
    void getUserProfileToolReturnsProfileWithSessionHistory() {
        var result = getUserProfile.apply(new McpToolsConfig.UserProfileRequest("user"));

        assertThat(result).isNotNull();
        TestUtils.assertJsonEquals(JsonUtils.toJsonString(result), """
                {
                  "username": "user",
                  "firstName": "John",
                  "lastName": "Doe",
                  "strengths": ["leadership", "communication"],
                  "values": ["integrity", "growth"],
                  "areaOfDevelopment": ["delegation", "time management"],
                  "longTermGoal": "Become a CTO",
                  "sessionHistory": [
                    {
                      "motivation": "Want to grow as a leader",
                      "reflection": "Made progress on delegation skills",
                      "areaOfDevelopment": ["delegation"],
                      "longTermGoal": "Become a CTO",
                      "actionSteps": [{"label": "Practice delegation daily", "checked": true}]
                    },
                    {
                      "motivation": "Focus on time management",
                      "reflection": "Delegation is improving, need to work on prioritization",
                      "areaOfDevelopment": ["delegation", "time management"],
                      "actionSteps": [{"label": "Use time blocking technique", "checked": false}]
                    }
                  ]
                }
                """);
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/mcp-tools-test.sql"})
    void getUserProfileToolReturnsNullForUnknownUser() {
        var result = getUserProfile.apply(new McpToolsConfig.UserProfileRequest("nonexistent"));
        assertThat(result).isNull();
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/mcp-tools-test.sql"})
    void searchCoachesToolReturnsCoachesWithAllFields() {
        var results = searchCoaches.apply(new McpToolsConfig.CoachSearchRequest("en", 10));

        assertThat(results).hasSize(1);
        TestUtils.assertJsonEquals(JsonUtils.toJsonString(results), """
                [{
                  "firstName": "Anna",
                  "lastName": "Coach",
                  "bio": "Executive coach specializing in leadership development",
                  "certificate": "[\\"PCC\\", \\"ACC\\"]",
                  "primaryRoles": "[\\"COACH\\", \\"MENTOR\\"]",
                  "fields": "[\\"leadership\\", \\"management\\"]",
                  "languages": "[\\"en\\", \\"cs\\"]"
                }]
                """);
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/mcp-tools-test.sql"})
    void getCoachByNameToolReturnsCoachDetail() {
        var result = getCoachByName.apply(new McpToolsConfig.CoachByNameRequest("Anna", "Coach"));

        assertThat(result).isPresent();
        TestUtils.assertJsonEquals(JsonUtils.toJsonString(result.get()), """
                {
                  "firstName": "Anna",
                  "lastName": "Coach",
                  "bio": "Executive coach specializing in leadership development",
                  "certificate": "[\\"PCC\\", \\"ACC\\"]",
                  "primaryRoles": "[\\"COACH\\", \\"MENTOR\\"]",
                  "fields": "[\\"leadership\\", \\"management\\"]"
                }
                """);
    }

    @Test
    @WithMockUser(username = "user", authorities = "USER")
    @Sql(scripts = {"/user_insight/mcp-tools-test.sql"})
    void getCoachByNameToolReturnsEmptyForUnknownCoach() {
        var result = getCoachByName.apply(new McpToolsConfig.CoachByNameRequest("Nonexistent", "Person"));
        assertThat(result).isEmpty();
    }


}
