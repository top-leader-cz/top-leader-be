package com.topleader.topleader.ai;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.user.User;
import com.topleader.topleader.util.common.user.UserUtils;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final ChatModel chatModel;

    private final ChatClient chatClient;

    private final AiPromptService aiPromptService;

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

    public String findLeadershipTip(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leadership tip for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_TIP);
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

    public String findActionsSteps(String locale, List<String> strengths, List<String> values, String areaOfDevelopment, String longTermGoal) {
        log.info("Finding actions steps for strengths: {} and values: {} locale: {} longTermGoal: {}", strengths, values, locale, areaOfDevelopment);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.ACTIONS_STEPS);
        return chatModel.call(String.format(prompt, strengths, values, areaOfDevelopment, longTermGoal, locale));
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

    @Retryable(
            value = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )

    public String generateUserPreviews(String username, List<String> actionsSteps) {
        log.info("AI api call for user previews. User:[{}], short term goals: {} ", username, actionsSteps);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_PREVIEWS);
        var query = String.format(prompt, actionsSteps);
        log.info("AI query: {}", query);

        var res = chatModel.call(query);
        log.info("AI user preview response: {}  User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }

    public String generateRecommendedGrowths(User user, String businessStrategy, String position, String competency) {
        var username = user.getUsername();
        log.info("AI api call for user generateRecommendedGrowths. User:[{}] ", username);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.RECOMMENDED_GROWTH);
        var query = String.format(prompt, businessStrategy, position, competency, UserUtils.localeToLanguage(user.getLocale()));
        log.info("generateRecommendedGrowths query: {} User:[{}]", query, username);

        var res = chatModel.call(query);
        log.info("AI user preview response: {} User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String generateUserArticles(String username, List<String> actionGoals, String language) {
        log.info("AI api call for user articles. User:[{}], short term goals: {} ", username, actionGoals);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_ARTICLES);
        var query = String.format(prompt, actionGoals, language);
        log.info("AI articles query: {}", query);

        var res = chatModel.call(query);
        log.info("AI user articles response: {}  User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }

    @Retryable(
            value = {Exception.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000, multiplier = 2)
    )
    public String generateSuggestion(String username, String userQuery, List<String> strengths, List<String> values, String language) {
        log.info("Findings Suggestions strengths: {} and values: {} locale: {}", strengths, values, language);
        var prompt = aiPromptService.prompt(AiPrompt.PromptType.SUGGESTION,
                Map.of("query", userQuery,
                "strengths",  String.join(", ", strengths),
                "values", String.join(", ", values),
                "language", language));
        log.info("Suggestions query: {}", prompt.getContents());

        var res = chatClient.prompt(prompt)
                .call()
                .content();
        log.info("Suggestions response: {}  User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }
}


