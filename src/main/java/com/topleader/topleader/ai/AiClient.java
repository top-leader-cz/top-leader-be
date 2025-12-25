package com.topleader.topleader.ai;


import com.topleader.topleader.user.User;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.util.common.user.UserUtils;
import io.github.resilience4j.retry.Retry;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.topleader.topleader.util.common.JsonUtils.MAPPER;


@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final ChatModel chatModel;

    private final ChatClient chatClient;

    private final AiPromptService aiPromptService;

    private final Retry retry;

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

    public String generateUserPreviews(String username, List<String> actionsSteps) {
        log.info("AI api call for user previews. User:[{}], short term goals: {} ", username, actionsSteps);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.USER_PREVIEWS);
        var query = String.format(prompt, actionsSteps);
        log.info("AI query: {}", query);

        var res = retry.executeSupplier(() -> chatModel.call(query));
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

        var res = retry.executeSupplier(() -> chatClient.prompt(prompt)
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

        var res = retry.executeSupplier(() -> chatClient.prompt(prompt)
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

        var res = retry.executeSupplier(() -> chatClient.prompt(prompt)
                .call()
                .content());
        log.info("Suggestions response: {}  User:[{}]", res, username);
        return AiUtils.replaceNonJsonString(res);
    }
}


