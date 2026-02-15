/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import com.topleader.topleader.TestUtils;
import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Supplier;

public class AiTestSettings {

    private static final String CHAT_COMPLETION_TEMPLATE = TestUtils.readFileAsString("ai/chat-completion-template.txt");
    private static final String TOOL_CALL_TEMPLATE = TestUtils.readFileAsString("ai/tool-call-template.txt");

    private final Map<String, Supplier<MockResponse>> stubResponses = new ConcurrentHashMap<>();
    private final List<AiStub> aiStubs = new CopyOnWriteArrayList<>();
    private final List<AiToolCallStub> aiToolCallStubs = new CopyOnWriteArrayList<>();

    public record AiStub(String bodyContains, String responseContent) {}
    public record AiToolCallStub(String bodyContains, String toolName, String toolArgs) {}

    public void stubResponse(String path, Supplier<MockResponse> responseSupplier) {
        stubResponses.put(path, responseSupplier);
    }

    public void stubAiResponse(String bodyContains, String responseContent) {
        aiStubs.add(new AiStub(bodyContains, responseContent));
    }

    public void stubAiToolCall(String bodyContains, String toolName, String toolArgs) {
        aiToolCallStubs.add(new AiToolCallStub(bodyContains, toolName, toolArgs));
    }

    public void resetAndInstall(MockWebServer mockServer) {
        stubResponses.clear();
        aiStubs.clear();
        aiToolCallStubs.clear();
        mockServer.setDispatcher(new Dispatcher() {
            @NotNull
            @Override
            public MockResponse dispatch(@NotNull RecordedRequest request) {
                var path = request.getPath();

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
    }

    private MockResponse handleChatCompletions(RecordedRequest request) {
        var body = request.getBody().readUtf8();
        var isToolFollowUp = body.contains("\"role\":\"tool\"");

        if (!isToolFollowUp) {
            var toolCallMatch = aiToolCallStubs.stream()
                    .filter(stub -> body.contains(stub.bodyContains()))
                    .findFirst();
            if (toolCallMatch.isPresent()) {
                return buildToolCallResponse(toolCallMatch.get().toolName(), toolCallMatch.get().toolArgs());
            }
        }

        return aiStubs.stream()
                .filter(stub -> body.contains(stub.bodyContains()))
                .findFirst()
                .map(stub -> buildContentResponse(stub.responseContent()))
                .orElseGet(() -> buildContentResponse(""));
    }

    private MockResponse buildContentResponse(String content) {
        var json = CHAT_COMPLETION_TEMPLATE.formatted(toJsonString(content), "stop");
        return jsonResponse(json);
    }

    private MockResponse buildToolCallResponse(String toolName, String toolArgs) {
        var json = TOOL_CALL_TEMPLATE.formatted(toolName, toJsonString(toolArgs));
        return jsonResponse(json);
    }

    private static String toJsonString(String value) {
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }

    private static MockResponse jsonResponse(String json) {
        return new MockResponse()
                .setResponseCode(200)
                .addHeader("Content-Type", "application/json")
                .setBody(json);
    }
}
