package com.topleader.topleader.common.ai;


import com.topleader.topleader.feedback.api.Summary;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.session.domain.UserPreview;
import com.topleader.topleader.common.util.common.user.UserUtils;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import org.apache.commons.lang3.StringUtils;
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
@RequiredArgsConstructor
public class AiClient {

    private final ChatClient chatClient;

    private final AiPromptService aiPromptService;

    private final RetryPolicy<Object> retryPolicy;


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

        var systemPrompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_PREVIEWS);

        var userMessage = "Short-term-goals: %s".formatted(String.join(", ", actionsSteps));

        log.info("User previews query: {}", userMessage);

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .toolNames("searchVideos")
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

        var userMessage = "Action goals: %s\nPreferred language: %s".formatted(
                String.join(", ", actionGoals), language);

        // Step 1: Search and select articles (fast - uses tool, minimal text output)
        var selectPrompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_ARTICLES);
        log.info("User articles selection query: {}", userMessage);

        var selected = Failsafe.with(retryPolicy).get(() -> chatClient.prompt()
                .system(selectPrompt)
                .user(userMessage)
                .toolNames("searchArticles")
                .call()
                .entity(new ParameterizedTypeReference<List<UserArticle>>() {}));
        log.info("User articles selected: {} articles for user [{}]", selected != null ? selected.size() : 0, username);

        if (selected == null || selected.isEmpty()) {
            return List.of();
        }

        // Build a map of URL -> imagePrompt from step 1 (step 2 may lose it)
        var imagePromptByUrl = selected.stream()
                .filter(a -> a.getUrl() != null && a.getImagePrompt() != null)
                .collect(java.util.stream.Collectors.toMap(UserArticle::getUrl, UserArticle::getImagePrompt, (a, b) -> a));

        // Step 2: Generate detailed summaries only for selected articles (no tools needed)
        var summaryPrompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_ARTICLES_SUMMARY);
        var articlesJson = selected.stream()
                .map(a -> "- \"%s\" by %s (%s) - %s".formatted(a.getTitle(), a.getAuthor(), a.getSource(), a.getUrl()))
                .collect(java.util.stream.Collectors.joining("\n"));
        var summaryMessage = "Articles to enrich:\n%s\n\nUser action goals: %s\nTarget language: %s".formatted(
                articlesJson, String.join(", ", actionGoals), language);

        log.info("User articles summary query for [{}]", username);

        var enriched = Failsafe.with(retryPolicy).get(() -> chatClient.prompt()
                .system(summaryPrompt)
                .user(summaryMessage)
                .call()
                .entity(new ParameterizedTypeReference<List<UserArticle>>() {}));
        log.info("User articles enriched: {} articles for user [{}]", enriched != null ? enriched.size() : 0, username);

        if (enriched == null || enriched.isEmpty()) {
            return selected;
        }

        // Restore imagePrompt from step 1 if step 2 lost it
        enriched.forEach(a -> {
            if (StringUtils.isBlank(a.getImagePrompt()) && a.getUrl() != null) {
                a.setImagePrompt(imagePromptByUrl.get(a.getUrl()));
            }
        });

        return enriched;
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


