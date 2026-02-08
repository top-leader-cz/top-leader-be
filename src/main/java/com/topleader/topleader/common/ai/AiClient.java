package com.topleader.topleader.common.ai;


import com.topleader.topleader.feedback.api.Summary;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.session.domain.UserPreview;
import com.topleader.topleader.common.util.common.user.UserUtils;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.topleader.topleader.common.util.common.JsonUtils.MAPPER;


@Slf4j
@Component
public class AiClient {

    private final ChatClient chatClient;
    private final AiPromptService aiPromptService;
    private final RetryPolicy<Object> retryPolicy;
    private final java.util.function.Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> searchArticles;
    private final java.util.function.Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> searchVideos;

    public AiClient(
            ChatClient chatClient,
            AiPromptService aiPromptService,
            RetryPolicy<Object> retryPolicy,
            @org.springframework.beans.factory.annotation.Qualifier("searchArticles")
            java.util.function.Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> searchArticles,
            @org.springframework.beans.factory.annotation.Qualifier("searchVideos")
            java.util.function.Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> searchVideos
    ) {
        this.chatClient = chatClient;
        this.aiPromptService = aiPromptService;
        this.retryPolicy = retryPolicy;
        this.searchArticles = searchArticles;
        this.searchVideos = searchVideos;
    }


    public String findLeaderShipStyle(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leadership style, locale: {}", locale);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.LEADERSHIP_STYLE,
                Map.of("strengths", String.join(", ", strengths),
                        "values", String.join(", ", values),
                        "language", locale));
        log.info("Leadership style query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .content());
        log.info("Leadership style response: {}", res);
        return res;
    }

    public String findAnimalSpirit(String locale, List<String> strengths, List<String> values) {
        log.info("Finding animal spirit, locale: {}", locale);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.ANIMAL_SPIRIT,
                Map.of("strengths", String.join(", ", strengths),
                        "values", String.join(", ", values),
                        "language", locale));
        log.info("Animal spirit query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .content());
        log.info("Animal spirit response: {}", res);
        return res;
    }

    public String findActionGoal(String locale, List<String> strengths, List<String> values, List<String> development, String longTermGoal, List<String> actionsSteps) {
        log.info("Finding personal growth tip, locale: {}", locale);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.PERSONAL_GROWTH_TIP,
                Map.of("strengths", String.join(", ", strengths),
                        "values", String.join(", ", values),
                        "areaOfDevelopment", String.join(", ", development),
                        "longTermGoal", longTermGoal,
                        "actionSteps", String.join(", ", actionsSteps),
                        "language", locale));
        log.info("Personal growth tip query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .content());
        log.info("Personal growth tip response: {}", res);
        return res;
    }

    public String findLeaderPersona(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leader persona, locale: {}", locale);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.WORLD_LEADER_PERSONA,
                Map.of("strengths", String.join(", ", strengths),
                        "values", String.join(", ", values),
                        "language", locale));
        log.info("Leader persona query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .content());
        log.info("Leader persona response: {}", res);
        return res;
    }

    public List<String> findLongTermGoal(String locale, List<String> strengths, List<String> values, String development) {
        log.info("Finding long term goal, locale: {}, development: {}", locale, development);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.LONG_TERM_GOALS,
                Map.of("strengths", String.join(", ", strengths),
                        "values", String.join(", ", values),
                        "areaOfDevelopment", development,
                        "language", locale));
        log.info("Long term goal query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {}));
        log.info("Long term goal response: {}", res);
        return res;
    }

    public List<String> findActionsSteps(String language, List<String> strengths, List<String> values, String areaOfDevelopment, String longTermGoal) {
        log.info("Finding action steps, language: {}, areaOfDevelopment: {}", language, areaOfDevelopment);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.ACTIONS_STEPS,
                Map.of("strengths", String.join(", ", strengths),
                        "values", String.join(", ", values),
                        "areaOfDevelopment", areaOfDevelopment,
                        "longTermGoal", longTermGoal,
                        "language", language));
        log.info("Action steps query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {}));
        log.info("Action steps response: {}", res);
        return res;
    }

    @SneakyThrows
    public Summary generateSummary(String locale, Map<String, List<String>> results) {
        log.info("Generating feedback summary, locale: {}", locale);
        var resultJson = MAPPER.writeValueAsString(results);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.FEEDBACK_SUMMARY,
                Map.of("resultJson", resultJson, "language", locale));
        log.info("Feedback summary query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .entity(Summary.class));
        log.info("Feedback summary response: {}", res);
        return res;
    }

    public List<UserPreview> generateUserPreviews(String username, List<String> actionsSteps) {
        log.info("Generating user previews, user: [{}]", username);

        var query = "TED talks YouTube " + String.join(", ", actionsSteps);
        log.info("[TAVILY] Searching videos: {}", query);
        var videoResults = searchVideos.apply(new McpToolsConfig.TavilySearchRequest(query));

        var resultsText = videoResults.stream()
                .map(r -> "- title: %s | url: %s | snippet: %s".formatted(r.title(), r.url(), r.content()))
                .collect(java.util.stream.Collectors.joining("\n"));

        var systemPrompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_PREVIEWS);
        var userMessage = "Short-term-goals: %s\n\nSearch results:\n%s".formatted(
                String.join(", ", actionsSteps), resultsText);

        log.info("User previews query for [{}], {} search results", username, videoResults.size());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .entity(new ParameterizedTypeReference<List<UserPreview>>() {}));
        log.info("User previews response: {}  User:[{}]", res, username);
        return res;
    }

    public List<RecommendedGrowth> generateRecommendedGrowths(User user, String businessStrategy, String position, String competency) {
        log.info("Generating recommended growths, user: [{}]", user.getUsername());
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.RECOMMENDED_GROWTH,
                Map.of("businessStrategy", businessStrategy,
                        "position", position,
                        "competency", competency,
                        "language", UserUtils.localeToLanguage(user.getLocale())));
        log.info("Recommended growths query: {} User:[{}]", prompt.getContents(), user.getUsername());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<RecommendedGrowth>>() {}));
        log.info("Recommended growths response: {} User:[{}]", res, user.getUsername());
        return res;
    }

    public List<UserArticle> generateUserArticles(String username, List<String> actionGoals, String language) {
        log.info("Generating user articles, user: [{}], language: {}", username, language);

        var query = String.join(", ", actionGoals) + " article " + language;
        log.info("[TAVILY] Searching articles: {}", query);
        var articleResults = searchArticles.apply(new McpToolsConfig.TavilySearchRequest(query));

        var resultsText = articleResults.stream()
                .map(r -> "- title: %s | url: %s | snippet: %s".formatted(r.title(), r.url(), r.content()))
                .collect(java.util.stream.Collectors.joining("\n"));

        var systemPrompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_ARTICLES);
        var userMessage = "Action goals: %s\nPreferred language: %s\n\nSearch results:\n%s".formatted(
                String.join(", ", actionGoals), language, resultsText);

        log.info("User articles query for [{}], {} search results", username, articleResults.size());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .call()
                .entity(new ParameterizedTypeReference<List<UserArticle>>() {}));
        log.info("User articles response: {} articles for user [{}]", res != null ? res.size() : 0, username);
        return res;
    }

    public String generateSuggestion(String username, String userQuery, List<String> strengths, List<String> values, String language) {
        log.info("Generating suggestion, user: [{}], language: {}", username, language);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.SUGGESTION,
                Map.of("query", userQuery,
                "strengths",  String.join(", ", strengths),
                "values", String.join(", ", values),
                "language", language));
        log.info("Suggestion query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .content());
        log.info("Suggestion response: {}  User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }

    public String generateSuggestionWithMcp(String username, String userQuery, String language) {
        log.info("Generating MCP suggestion, user: [{}], language: {}", username, language);

        var suggestionPrompt = aiPromptService.getPrompt(AiPrompt.PromptType.SUGGESTION);

        var toolInstructions = """

            TOOL INSTRUCTIONS:
            You have access to tools to enrich your response. Always use them before generating your answer.

            1. ALWAYS call getUserProfile with the user's username to get their real strengths, values, development areas, long-term goal, and session history.
               Use this data instead of the placeholders in the prompt above.
            2. If the user's concern relates to coaching, mentoring, or personal development, call searchCoaches to find relevant coaches.
            3. When recommending coaches, evaluate these attributes against the user's profile:
               - bio: coach's description and coaching philosophy
               - certificate: coaching certifications and qualifications
               - primaryRoles: specialization (COACH, MENTOR, THERAPIST)
               - fields: areas of expertise
               Explain why each recommended coach is a good fit for this specific user.
            4. NEVER include any usernames, email addresses, or internal identifiers in your response.
               Always refer to coaches by their first and last name only.
            """;

        var systemPrompt = suggestionPrompt + toolInstructions;

        var userMessage = "My username is: %s\nPreferred language: %s\n\n%s".formatted(username, language, userQuery);

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .toolNames("getUserProfile", "searchCoaches", "getCoachByName")
                .call()
                .content());
        log.info("MCP Suggestion response: {}  User:[{}]", res, username);
        return res;
    }
}


