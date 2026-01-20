package com.topleader.topleader.config.stub;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * Registry for configuring AI stub responses in tests.
 * Use this instead of Mockito.when() for ChatModel and ChatClient stubs.
 *
 * <p>Example usage in tests:
 * <pre>
 * {@code
 * @Autowired
 * AiStubRegistry aiStubRegistry;
 *
 * @BeforeEach
 * void setUp() {
 *     aiStubRegistry.reset();
 * }
 *
 * @Test
 * void myTest() {
 *     aiStubRegistry.stubChatModelResponse("my query", "my response");
 *     aiStubRegistry.stubChatClientEntity(List.of(new MyDto()));
 *     // ... test code
 * }
 * }
 * </pre>
 */
@Component
public class AiStubRegistry {

    private final Map<String, String> chatModelResponses = new ConcurrentHashMap<>();
    private String defaultChatModelResponse = "Test AI response";
    private Object chatClientEntityResponse = null;
    private String chatClientContentResponse = "Test AI response";
    private Function<String, String> chatModelResponseFunction = null;

    /**
     * Reset all stubs to default state. Call this in @BeforeEach.
     */
    public void reset() {
        chatModelResponses.clear();
        defaultChatModelResponse = "Test AI response";
        chatClientEntityResponse = null;
        chatClientContentResponse = "Test AI response";
        chatModelResponseFunction = null;
    }

    /**
     * Stub ChatModel.call(query) to return specific response for exact query match.
     */
    public void stubChatModelResponse(String query, String response) {
        chatModelResponses.put(query, response);
    }

    /**
     * Set default response for ChatModel when no specific stub matches.
     */
    public void setDefaultChatModelResponse(String response) {
        this.defaultChatModelResponse = response;
    }

    /**
     * Set a function to compute ChatModel response based on query.
     * Useful for dynamic responses.
     */
    public void setChatModelResponseFunction(Function<String, String> function) {
        this.chatModelResponseFunction = function;
    }

    /**
     * Get response for ChatModel.call(query).
     * First checks exact match, then function, then default.
     */
    public String getChatModelResponse(String query) {
        // Check exact match first
        var exactMatch = chatModelResponses.get(query);
        if (exactMatch != null) {
            return exactMatch;
        }

        // Check function
        if (chatModelResponseFunction != null) {
            return chatModelResponseFunction.apply(query);
        }

        // Return default
        return defaultChatModelResponse;
    }

    /**
     * Stub ChatClient entity response for typed entity calls.
     */
    public <T> void stubChatClientEntity(T response) {
        this.chatClientEntityResponse = response;
    }

    /**
     * Stub ChatClient content() response.
     */
    public void stubChatClientContent(String response) {
        this.chatClientContentResponse = response;
    }

    /**
     * Get stubbed content response for ChatClient.
     */
    public String getChatClientContentResponse() {
        return chatClientContentResponse;
    }

    /**
     * Get stubbed entity response for ChatClient.
     */
    @SuppressWarnings("unchecked")
    public <T> T getChatClientEntityResponse(ParameterizedTypeReference<T> type) {
        return (T) chatClientEntityResponse;
    }

    @SuppressWarnings("unchecked")
    public <T> T getChatClientEntityResponse(Class<T> type) {
        return (T) chatClientEntityResponse;
    }
}
