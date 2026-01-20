package com.topleader.topleader.config.stub;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.List;

/**
 * Stub implementation of ChatModel for native image compatible testing.
 * Uses AiStubRegistry for configurable responses.
 */
@Slf4j
public class StubChatModel implements ChatModel {

    private final AiStubRegistry registry;

    public StubChatModel(AiStubRegistry registry) {
        this.registry = registry;
    }

    @Override
    public String call(String message) {
        var response = registry.getChatModelResponse(message);
        log.debug("StubChatModel.call() - message: {}, response: {}",
                message.substring(0, Math.min(100, message.length())), response);
        return response;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        var messageContent = prompt.getContents();
        var response = registry.getChatModelResponse(messageContent);
        var message = new AssistantMessage(response);
        var generation = new Generation(message);
        return new ChatResponse(List.of(generation));
    }

    @Override
    public ChatOptions getDefaultOptions() {
        return null;
    }
}
