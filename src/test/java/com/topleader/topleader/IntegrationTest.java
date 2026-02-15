/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.topleader.topleader.configuration.TestBeanConfiguration;
import com.topleader.topleader.configuration.EnablePostgresTestContainerContextCustomizerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


/**
 * @author Daniel Slavik
 */

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
        TopLeaderApplication.class,
        TestBeanConfiguration.class})
@TestExecutionListeners(mergeMode =
    TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
    listeners = {ResetDatabaseAfterTestMethodListener.class}
)
@EnablePostgresTestContainerContextCustomizerFactory.EnabledPostgresTestContainer
@DisabledInAotMode
public abstract class IntegrationTest implements ApplicationContextAware {

    protected MockMvc mvc;

    @Autowired
    protected GreenMail greenMail;

    @Autowired
    protected MockWebServer mockServer;

    @Autowired
    protected ObjectMapper objectMapper;

    private final Map<String, Supplier<MockResponse>> stubResponses = new ConcurrentHashMap<>();
    private final List<AiStub> aiStubs = new CopyOnWriteArrayList<>();
    private final List<AiToolCallStub> aiToolCallStubs = new CopyOnWriteArrayList<>();

    protected record AiStub(String bodyContains, String responseContent) {}
    protected record AiToolCallStub(String bodyContains, String toolName, String toolArgs) {}

    protected void stubResponse(String path, Supplier<MockResponse> responseSupplier) {
        stubResponses.put(path, responseSupplier);
    }

    protected void stubAiResponse(String bodyContains, String responseContent) {
        aiStubs.add(new AiStub(bodyContains, responseContent));
    }

    protected void stubAiToolCall(String bodyContains, String toolName, String toolArgs) {
        aiToolCallStubs.add(new AiToolCallStub(bodyContains, toolName, toolArgs));
    }

    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) applicationContext)
            .apply(springSecurity())
            .build();

    }

    @BeforeEach
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        stubResponses.clear();
        aiStubs.clear();
        aiToolCallStubs.clear();
        mockServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                var path = request.getPath();

                // Handle OpenAI chat completions
                if (path != null && path.startsWith("/v1/chat/completions")) {
                    return handleChatCompletions(request);
                }

                return stubResponses.entrySet().stream()
                        .filter(e -> path != null && path.startsWith(e.getKey()))
                        .map(e -> e.getValue().get())
                        .findFirst()
                        .orElse(new MockResponse().setResponseCode(404));
            }
        });

        if(!greenMail.isRunning()) {
            greenMail.start();
        }
    }

    @AfterEach
    public void testCleanUp() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    private MockResponse handleChatCompletions(RecordedRequest request) {
        var body = request.getBody().readUtf8();

        // Check if this is a tool-call follow-up (contains role:tool)
        var isToolFollowUp = body.contains("\"role\":\"tool\"");

        // If NOT a tool follow-up, check for tool call stubs first
        if (!isToolFollowUp) {
            for (var stub : aiToolCallStubs) {
                if (body.contains(stub.bodyContains())) {
                    return buildToolCallResponse(stub.toolName(), stub.toolArgs());
                }
            }
        }

        // Match against content stubs
        for (var stub : aiStubs) {
            if (body.contains(stub.bodyContains())) {
                return buildContentResponse(stub.responseContent());
            }
        }

        // Default fallback - return empty content
        return buildContentResponse("");
    }

    private MockResponse buildContentResponse(String content) {
        try {
            var id = "chatcmpl-" + UUID.randomUUID().toString().substring(0, 8);
            var message = objectMapper.createObjectNode()
                    .put("role", "assistant")
                    .put("content", content);
            var choice = objectMapper.createObjectNode()
                    .put("index", 0)
                    .put("finish_reason", "stop");
            choice.set("message", message);
            var choices = objectMapper.createArrayNode().add(choice);
            var usage = objectMapper.createObjectNode()
                    .put("prompt_tokens", 10)
                    .put("completion_tokens", 10)
                    .put("total_tokens", 20);
            var root = objectMapper.createObjectNode()
                    .put("id", id)
                    .put("object", "chat.completion")
                    .put("model", "gpt-5.2");
            root.set("choices", choices);
            root.set("usage", usage);

            return new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(objectMapper.writeValueAsString(root));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build AI content response", e);
        }
    }

    private MockResponse buildToolCallResponse(String toolName, String toolArgs) {
        try {
            var id = "chatcmpl-" + UUID.randomUUID().toString().substring(0, 8);
            var callId = "call_" + UUID.randomUUID().toString().substring(0, 8);
            var function = objectMapper.createObjectNode()
                    .put("name", toolName)
                    .put("arguments", toolArgs);
            var toolCall = objectMapper.createObjectNode()
                    .put("id", callId)
                    .put("type", "function");
            toolCall.set("function", function);
            var toolCalls = objectMapper.createArrayNode().add(toolCall);
            var message = objectMapper.createObjectNode()
                    .put("role", "assistant")
                    .putNull("content");
            message.set("tool_calls", toolCalls);
            var choice = objectMapper.createObjectNode()
                    .put("index", 0)
                    .put("finish_reason", "tool_calls");
            choice.set("message", message);
            var choices = objectMapper.createArrayNode().add(choice);
            var usage = objectMapper.createObjectNode()
                    .put("prompt_tokens", 10)
                    .put("completion_tokens", 10)
                    .put("total_tokens", 20);
            var root = objectMapper.createObjectNode()
                    .put("id", id)
                    .put("object", "chat.completion")
                    .put("model", "gpt-5.2");
            root.set("choices", choices);
            root.set("usage", usage);

            return new MockResponse()
                    .setResponseCode(200)
                    .addHeader("Content-Type", "application/json")
                    .setBody(objectMapper.writeValueAsString(root));
        } catch (Exception e) {
            throw new RuntimeException("Failed to build AI tool call response", e);
        }
    }


}
