package com.topleader.topleader.program.recommendation;

import com.topleader.topleader.IntegrationTest;
import com.topleader.topleader.StubFunction;
import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.program.participant.ProgramParticipantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql({"/sql/participant/participant-test.sql", "/sql/recommendation/program-recommendation-prompts.sql"})
class ProgramRecommendationIT extends IntegrationTest {

    private static final String ARTICLES_JSON = """
            [
              {
                "title": "How to give better feedback",
                "url": "https://example.com/feedback",
                "readTime": "8 min"
              },
              {
                "title": "Radical candor in practice",
                "url": "https://example.com/candor",
                "readTime": "12 min"
              }
            ]
            """;

    private static final String VIDEOS_JSON = """
            [
              {
                "title": "The secret to giving great feedback",
                "url": "https://youtube.com/watch?v=feedback1",
                "length": "11 min",
                "thumbnail": "https://img.youtube.com/feedback1.jpg"
              }
            ]
            """;

    @Autowired
    private ProgramRecommendationService recommendationService;

    @Autowired
    private ProgramParticipantRepository participantRepository;

    @Autowired
    private ProgramParticipantRecommendationRepository recommendationRepository;

    @Autowired
    @Qualifier("searchArticles")
    private StubFunction<AiClient.TavilySearchRequest, List<AiClient.TavilySearchResult>> mockSearchArticles;

    @Autowired
    @Qualifier("searchVideos")
    private StubFunction<AiClient.TavilySearchRequest, List<AiClient.TavilySearchResult>> mockSearchVideos;

    @BeforeEach
    void resetSearchStubs() {
        mockSearchArticles.reset();
        mockSearchVideos.reset();
    }

    @Test
    void loadForParticipant_returnsEmpty_whenNoRecommendations() {
        var participant = participantRepository.findById(2L).orElseThrow();

        var result = recommendationService.loadForParticipant(participant, "en");

        assertThat(result.articles()).isEmpty();
        assertThat(result.videos()).isEmpty();
        assertThat(result.isEmpty()).isTrue();
        assertThat(result.focusAreaLabel()).isEqualTo("Giving feedback");
    }

    @Test
    void generateAsync_persistsArticlesAndVideos() {
        stubTavilySearches();
        stubAiResponse("article", ARTICLES_JSON);
        stubAiResponse("video", VIDEOS_JSON);

        var participant = participantRepository.findById(2L).orElseThrow();
        recommendationService.generateAsync(participant, "en");

        var rows = recommendationRepository.findByParticipantAndCycle(participant.getId(), participant.getCurrentCycle());
        assertThat(rows).hasSize(3);

        var articles = rows.stream()
                .filter(r -> r.getType() == ProgramParticipantRecommendation.Type.ARTICLE)
                .toList();
        assertThat(articles).hasSize(2);
        assertThat(articles).extracting(ProgramParticipantRecommendation::getRelevanceRank)
                .containsExactlyInAnyOrder(1, 2);
        assertThat(articles).allSatisfy(r -> {
            assertThat(r.getProgramParticipantId()).isEqualTo(2L);
            assertThat(r.getCycle()).isEqualTo(1);
            assertThat(r.getCreatedAt()).isNotNull();
            assertThat(r.getContent().json()).contains("title");
        });

        var videos = rows.stream()
                .filter(r -> r.getType() == ProgramParticipantRecommendation.Type.VIDEO)
                .toList();
        assertThat(videos).hasSize(1);
        assertThat(videos.get(0).getRelevanceRank()).isEqualTo(1);
        assertThat(videos.get(0).getContent().json()).contains("youtube.com");
    }

    @Test
    void generateAsync_replacesExistingRecommendationsForSameCycle() {
        stubTavilySearches();
        stubAiResponse("article", ARTICLES_JSON);
        stubAiResponse("video", VIDEOS_JSON);

        var participant = participantRepository.findById(2L).orElseThrow();
        recommendationService.generateAsync(participant, "en");
        recommendationService.generateAsync(participant, "en");

        var rows = recommendationRepository.findByParticipantAndCycle(participant.getId(), participant.getCurrentCycle());
        // 2 articles + 1 video, not duplicated
        assertThat(rows).hasSize(3);
    }

    @Test
    void generateAsync_persistsNothing_whenTavilyEmpty() {
        // mockSearchArticles / mockSearchVideos default to empty list -> AiClient returns empty
        var participant = participantRepository.findById(2L).orElseThrow();
        recommendationService.generateAsync(participant, "en");

        var rows = recommendationRepository.findByParticipantAndCycle(participant.getId(), participant.getCurrentCycle());
        assertThat(rows).isEmpty();
    }

    @Test
    void generateAsync_swallowsExceptions() {
        // Tavily stub throws -> service must not propagate
        mockSearchArticles.returns(null);
        mockSearchVideos.returns(null);

        var participant = participantRepository.findById(2L).orElseThrow();
        recommendationService.generateAsync(participant, "en"); // must not throw

        var rows = recommendationRepository.findByParticipantAndCycle(participant.getId(), participant.getCurrentCycle());
        assertThat(rows).isEmpty();
    }

    @Test
    void loadForParticipant_returnsPersistedRecommendations() {
        stubTavilySearches();
        stubAiResponse("article", ARTICLES_JSON);
        stubAiResponse("video", VIDEOS_JSON);

        var participant = participantRepository.findById(2L).orElseThrow();
        recommendationService.generateAsync(participant, "en");

        var result = recommendationService.loadForParticipant(participant, "en");

        assertThat(result.focusAreaLabel()).isEqualTo("Giving feedback");
        assertThat(result.articles()).hasSize(2);
        assertThat(result.articles()).allSatisfy(a -> {
            assertThat(a.type()).isEqualTo("ARTICLE");
            assertThat(a.title()).isNotBlank();
            assertThat(a.url()).startsWith("https://example.com/");
            assertThat(a.duration()).contains("min");
            assertThat(a.thumbnailUrl()).isNull();
        });
        assertThat(result.videos()).hasSize(1);
        var video = result.videos().get(0);
        assertThat(video.type()).isEqualTo("VIDEO");
        assertThat(video.title()).isEqualTo("The secret to giving great feedback");
        assertThat(video.url()).isEqualTo("https://youtube.com/watch?v=feedback1");
        assertThat(video.duration()).isEqualTo("11 min");
        assertThat(video.thumbnailUrl()).isEqualTo("https://img.youtube.com/feedback1.jpg");
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void dashboard_includesLearnMore_whenRecommendationsExist() throws Exception {
        stubTavilySearches();
        stubAiResponse("article", ARTICLES_JSON);
        stubAiResponse("video", VIDEOS_JSON);

        var participant = participantRepository.findById(2L).orElseThrow();
        recommendationService.generateAsync(participant, "en");

        var result = mvc.perform(get("/api/latest/participant/programs/1/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertThat(result).contains("\"learnMore\"");
        assertThat(result).contains("\"focusAreaLabel\":\"Giving feedback\"");
        assertThat(result).contains("How to give better feedback");
        assertThat(result).contains("The secret to giving great feedback");
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void dashboard_omitsLearnMore_whenNoRecommendations() throws Exception {
        var result = mvc.perform(get("/api/latest/participant/programs/1/dashboard"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        // When learnMore is empty, DashboardDto serializes it as null
        assertThat(result).contains("\"learnMore\":null");
    }

    @Test
    @WithMockUser(username = "participant1", authorities = "USER")
    void submitBaseline_triggersRecommendationGeneration() throws Exception {
        stubTavilySearches();
        stubAiResponse("weekly practice", """
                ["Practice 1", "Practice 2", "Practice 3", "Practice 4", "Practice 5"]
                """);
        stubAiResponse("article", ARTICLES_JSON);
        stubAiResponse("video", VIDEOS_JSON);

        mvc.perform(post("/api/latest/participant/programs/1/enroll")
                        .contentType("application/json")
                        .content("""
                                { "focusArea": "fa.giving-feedback", "personalGoal": "test" }
                                """))
                .andExpect(status().isOk());

        mvc.perform(post("/api/latest/participant/programs/1/assessment/baseline")
                        .contentType("application/json")
                        .content("""
                                { "q1": 3, "q2": 4, "q3": 2, "q4": 5, "q5": 3 }
                                """))
                .andExpect(status().isOk());

        var rows = recommendationRepository.findByParticipantAndCycle(1L, 1);
        assertThat(rows).hasSize(3);
    }

    @Test
    void loadForParticipant_returnsLocalizedLabel_forCzech() {
        var participant = participantRepository.findById(2L).orElseThrow();

        var result = recommendationService.loadForParticipant(participant, "cs");

        assertThat(result.focusAreaLabel()).isEqualTo("Poskytování zpětné vazby");
    }

    @Test
    void loadForParticipant_returnsLocalizedLabel_forGerman() {
        var participant = participantRepository.findById(2L).orElseThrow();

        var result = recommendationService.loadForParticipant(participant, "de");

        assertThat(result.focusAreaLabel()).isEqualTo("Feedback geben");
    }

    @Test
    void loadForParticipant_returnsLocalizedLabel_forFrench() {
        var participant = participantRepository.findById(2L).orElseThrow();

        var result = recommendationService.loadForParticipant(participant, "fr");

        assertThat(result.focusAreaLabel()).isEqualTo("Donner du feedback");
    }

    @Test
    @WithMockUser(username = "participant2", authorities = "USER")
    void updateGoal_regeneratesRecommendations() throws Exception {
        stubTavilySearches();
        stubAiResponse("article", ARTICLES_JSON);
        stubAiResponse("video", VIDEOS_JSON);

        mvc.perform(put("/api/latest/participant/programs/1/goal")
                        .contentType("application/json")
                        .content("""
                                { "goal": "Become a more empathetic leader for my team" }
                                """))
                .andExpect(status().isOk());

        var rows = recommendationRepository.findByParticipantAndCycle(2L, 1);
        assertThat(rows).hasSize(3);
        assertThat(rows).extracting(r -> r.getContent().json())
                .anyMatch(json -> json.contains("How to give better feedback"));

        var participant = participantRepository.findById(2L).orElseThrow();
        assertThat(participant.getPersonalGoal()).isEqualTo("Become a more empathetic leader for my team");
    }

    private void stubTavilySearches() {
        mockSearchArticles.returns(List.of(
                new AiClient.TavilySearchResult("Feedback article", "https://example.com/feedback", "How to give feedback")
        ));
        mockSearchVideos.returns(List.of(
                new AiClient.TavilySearchResult("Feedback video", "https://youtube.com/watch?v=feedback1", "Great feedback talk")
        ));
    }
}
