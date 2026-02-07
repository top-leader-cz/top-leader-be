package com.topleader.topleader.common.ai;


import com.topleader.topleader.user.User;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.common.util.common.user.UserUtils;
import dev.failsafe.Failsafe;
import dev.failsafe.RetryPolicy;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.topleader.topleader.common.util.common.JsonUtils.MAPPER;


@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final ChatModel chatModel;

    private final ChatClient chatClient;

    private final AiPromptService aiPromptService;

    private final RetryPolicy<Object> retryPolicy;


    public String findLeaderShipStyle(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leadership style for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_STYLE);
        return chatModel.call(String.format(prompt, strengths, values, locale));
    }

    public String findAnimalSpirit(String locale, List<String> strengths, List<String> values) {
        log.info("Finding animal spirit for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.ANIMAL_SPIRIT);
        return chatModel.call(String.format(prompt, strengths, values, locale));
    }

    public String findActionGoal(String locale, List<String> strengths, List<String> values, List<String> development, String longTermGoal, List<String> actionsSteps) {
        log.info("Finding personal growth for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.PERSONAL_GROWTH_TIP);
        var query = String.format(prompt, strengths, values, development, longTermGoal, actionsSteps, locale);
        log.info("query: {}", query);
        return chatModel.call(query);
    }

    public String findLeaderPersona(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leader persona for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.WORLD_LEADER_PERSONA);
        return chatModel.call(String.format(prompt, strengths, values, locale));
    }

    public String findLongTermGoal(String locale, List<String> strengths, List<String> values, String development) {
        log.info("Finding long term goal for strengths: {} and values: {} locale: {} longTermGoal: {}", strengths, values, locale, development);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.LONG_TERM_GOALS);
        return chatModel.call(String.format(prompt, strengths, values, development, locale));
    }

    public List<String> findActionsSteps(String language, List<String> strengths, List<String> values, String areaOfDevelopment, String longTermGoal) {
        log.info("Finding actions steps for strengths: {} and values: {} language: {} areaOfDevelopment: {}", strengths, values, language, areaOfDevelopment);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.ACTIONS_STEPS,
                Map.of("strengths", String.join(", ", strengths),
                        "values", String.join(", ", values),
                        "areaOfDevelopment", areaOfDevelopment,
                        "longTermGoal", longTermGoal,
                        "language", language));
        log.info("Actions steps query: {}", prompt.getContents());
        return Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<String>>() {}));
    }

    @SneakyThrows
    public String generateSummary(String locale, Map<String, List<String>> results) {
        log.info("AI api call for summary {}  locale: {},", results, locale);
        var resultJson = MAPPER.writeValueAsString(results);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.FEEDBACK_SUMMARY);

        var aiQuery = String.format(prompt, resultJson, locale);
        log.debug("AI query for summary {}  locale: {},", aiQuery, locale);

        var res = chatModel.call(aiQuery);
        log.info("AI Summary response: {},", res);
        return AiUtils.replaceNonJsonString(res);
    }

    public String generateUserPreviews(String username, List<String> actionsSteps) {
        log.info("AI api call for user previews. User:[{}], short term goals: {} ", username, actionsSteps);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_PREVIEWS);
        var query = String.format(prompt, actionsSteps);
        log.info("AI query: {}", query);

        var res = Failsafe.with(retryPolicy).get(() -> chatModel.call(query));
        log.info("AI user preview response: {}  User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }

    public List<RecommendedGrowth> generateRecommendedGrowths(User user, String businessStrategy, String position, String competency) {
        var username = user.getUsername();
        log.info("AI api call for user generateRecommendedGrowths. User:[{}] ", username);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.RECOMMENDED_GROWTH,
                Map.of("businessStrategy", businessStrategy,
                        "position", position,
                        "competency", competency,
                        "language", UserUtils.localeToLanguage(user.getLocale())));
        log.info("generateRecommendedGrowths query: {} User:[{}]", prompt.getContents(), username);

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<RecommendedGrowth>>() {}));
        log.info("AI generateRecommendedGrowths response: {} User:[{}]", res, username);
        return res;
    }

    public List<UserArticle> generateUserArticles(String username, List<String> actionGoals, String language) {
        log.info("AI api call for user articles. User:[{}], short term goals: {} ", username, actionGoals);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.USER_ARTICLES,
                Map.of("actionGoals", String.join(", ", actionGoals),
                        "language", language));
        log.info("AI articles query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .entity(new ParameterizedTypeReference<List<UserArticle>>() {}));
        log.info("AI user articles response: {}  User:[{}]", res, username);
        return res;
    }

    public String generateSuggestion(String username, String userQuery, List<String> strengths, List<String> values, String language) {
        log.info("Findings Suggestions strengths: {} and values: {} locale: {}", strengths, values, language);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.SUGGESTION,
                Map.of("query", userQuery,
                "strengths",  String.join(", ", strengths),
                "values", String.join(", ", values),
                "language", language));
        log.info("Suggestions query: {}", prompt.getContents());

        var res = Failsafe.with(retryPolicy).get(() -> chatClient.prompt(prompt)
                .call()
                .content());
        log.info("Suggestions response: {}  User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }

    public String generateSuggestionWithMcp(String username, String userQuery, String language) {
        log.info("Generating MCP-powered suggestion for user: {} query: {}", username, userQuery);

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

        log.info("MCP Suggestion response for user {}: {}", username, res);
        return res;
    }
}


