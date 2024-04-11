package com.topleader.topleader.ai;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.ChatClient;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class AiClient {

    private final ChatClient chatClient;

    private final AiPromptService aiPromptService;

    public String findLeaderShipStyle(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leadership style for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_STYLE);
        return chatClient.call(String.format(prompt, strengths, values, locale));
    }

    public String findAnimalSpirit(String locale, List<String> strengths, List<String> values) {
        log.info("Finding animal spirit for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.ANIMAL_SPIRIT);
        return chatClient.call(String.format(prompt, strengths, values, locale));
    }

    public String findLeadershipTip(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leadership tip for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.LEADERSHIP_TIP);
        return chatClient.call(String.format(prompt, strengths, values, locale));
    }

    public String findActionGoal(String locale, List<String> strengths, List<String> values, List<String> development, String longTermGoal, List<String> actionsSteps) {
        log.info("Finding personal growth for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.PERSONAL_GROWTH_TIP);
        var query = MessageFormat.format(prompt, strengths, values, development, longTermGoal, actionsSteps, locale);
        log.info("query: {}", query);
        return chatClient.call(query);
    }

    public String findLeaderPersona(String locale, List<String> strengths, List<String> values) {
        log.info("Finding leader persona for strengths: {} and values: {} locale: {}", strengths, values, locale);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.WORLD_LEADER_PERSONA);
        return chatClient.call(String.format(prompt, strengths, values, locale));
    }

    public String findLongTermGoal(String locale, List<String> strengths, List<String> values, String development) {
        log.info("Finding long term goal for strengths: {} and values: {} locale: {} longTermGoal: {}", strengths, values, locale, development);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.LONG_TERM_GOALS);
        return chatClient.call(String.format(prompt, strengths, values, development, locale));
    }

    public String findActionsSteps(String locale, List<String> strengths, List<String> values, String areaOfDevelopment, String longTermGoal) {
        log.info("Finding actions steps for strengths: {} and values: {} locale: {} longTermGoal: {}", strengths, values, locale, areaOfDevelopment);
        var prompt = aiPromptService.getPrompt(AiPrompt.PromptType.ACTIONS_STEPS);
        return chatClient.call(String.format(prompt, strengths, values, areaOfDevelopment, longTermGoal, locale));
    }
}


