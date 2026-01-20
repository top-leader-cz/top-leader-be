package com.topleader.topleader.config.stub;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ResponseEntity;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.template.TemplateRenderer;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.Resource;
import reactor.core.publisher.Flux;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Stub implementation of ChatClient for native image compatible testing.
 * Uses AiStubRegistry for configurable responses.
 */
public class StubChatClient implements ChatClient {

    private final AiStubRegistry registry;

    public StubChatClient(AiStubRegistry registry) {
        this.registry = registry;
    }

    @Override
    public ChatClientRequestSpec prompt() {
        return new StubRequestSpec(registry);
    }

    @Override
    public ChatClientRequestSpec prompt(Prompt prompt) {
        return new StubRequestSpec(registry);
    }

    @Override
    public ChatClientRequestSpec prompt(String content) {
        return new StubRequestSpec(registry);
    }

    @Override
    public Builder mutate() {
        return null;
    }

    private static class StubRequestSpec implements ChatClientRequestSpec {
        private final AiStubRegistry registry;

        StubRequestSpec(AiStubRegistry registry) {
            this.registry = registry;
        }

        @Override
        public CallResponseSpec call() {
            return new StubCallResponseSpec(registry);
        }

        @Override
        public StreamResponseSpec stream() {
            return new StubStreamResponseSpec(registry);
        }

        @Override public Builder mutate() { return null; }
        @Override public ChatClientRequestSpec advisors(Consumer<AdvisorSpec> consumer) { return this; }
        @Override public ChatClientRequestSpec advisors(Advisor... advisors) { return this; }
        @Override public ChatClientRequestSpec advisors(List<Advisor> advisors) { return this; }
        @Override public ChatClientRequestSpec messages(Message... messages) { return this; }
        @Override public ChatClientRequestSpec messages(List<Message> messages) { return this; }
        @Override public <T extends ChatOptions> ChatClientRequestSpec options(T options) { return this; }
        @Override public ChatClientRequestSpec toolNames(String... toolNames) { return this; }
        @Override public ChatClientRequestSpec tools(Object... tools) { return this; }
        @Override public ChatClientRequestSpec toolCallbacks(ToolCallback... toolCallbacks) { return this; }
        @Override public ChatClientRequestSpec toolCallbacks(List<ToolCallback> toolCallbacks) { return this; }
        @Override public ChatClientRequestSpec toolCallbacks(ToolCallbackProvider... toolCallbackProviders) { return this; }
        @Override public ChatClientRequestSpec toolContext(Map<String, Object> toolContext) { return this; }
        @Override public ChatClientRequestSpec system(String text) { return this; }
        @Override public ChatClientRequestSpec system(Resource resource, Charset charset) { return this; }
        @Override public ChatClientRequestSpec system(Resource resource) { return this; }
        @Override public ChatClientRequestSpec system(Consumer<PromptSystemSpec> consumer) { return this; }
        @Override public ChatClientRequestSpec user(String text) { return this; }
        @Override public ChatClientRequestSpec user(Resource resource, Charset charset) { return this; }
        @Override public ChatClientRequestSpec user(Resource resource) { return this; }
        @Override public ChatClientRequestSpec user(Consumer<PromptUserSpec> consumer) { return this; }
        @Override public ChatClientRequestSpec templateRenderer(TemplateRenderer renderer) { return this; }
    }

    private static class StubCallResponseSpec implements CallResponseSpec {
        private final AiStubRegistry registry;

        StubCallResponseSpec(AiStubRegistry registry) {
            this.registry = registry;
        }

        @Override
        public String content() {
            return registry.getChatClientContentResponse();
        }

        @Override
        public ChatResponse chatResponse() {
            var message = new AssistantMessage(registry.getChatClientContentResponse());
            var generation = new Generation(message);
            return new ChatResponse(List.of(generation));
        }

        @Override
        public org.springframework.ai.chat.client.ChatClientResponse chatClientResponse() {
            return null;
        }

        @Override
        public <T> T entity(Class<T> type) {
            return registry.getChatClientEntityResponse(type);
        }

        @Override
        public <T> T entity(ParameterizedTypeReference<T> type) {
            return registry.getChatClientEntityResponse(type);
        }

        @Override
        public <T> T entity(org.springframework.ai.converter.StructuredOutputConverter<T> converter) {
            return null;
        }

        @Override
        public <T> ResponseEntity<ChatResponse, T> responseEntity(Class<T> type) {
            T entity = registry.getChatClientEntityResponse(type);
            ChatResponse response = chatResponse();
            return new ResponseEntity<>(response, entity);
        }

        @Override
        public <T> ResponseEntity<ChatResponse, T> responseEntity(ParameterizedTypeReference<T> type) {
            T entity = registry.getChatClientEntityResponse(type);
            ChatResponse response = chatResponse();
            return new ResponseEntity<>(response, entity);
        }

        @Override
        public <T> ResponseEntity<ChatResponse, T> responseEntity(org.springframework.ai.converter.StructuredOutputConverter<T> converter) {
            return null;
        }
    }

    private static class StubStreamResponseSpec implements StreamResponseSpec {
        private final AiStubRegistry registry;

        StubStreamResponseSpec(AiStubRegistry registry) {
            this.registry = registry;
        }

        @Override
        public Flux<String> content() {
            return Flux.just(registry.getChatClientContentResponse());
        }

        @Override
        public Flux<ChatResponse> chatResponse() {
            var message = new AssistantMessage(registry.getChatClientContentResponse());
            var generation = new Generation(message);
            return Flux.just(new ChatResponse(List.of(generation)));
        }

        @Override
        public Flux<org.springframework.ai.chat.client.ChatClientResponse> chatClientResponse() {
            return Flux.empty();
        }
    }
}
