package com.topleader.topleader.config;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.topleader.topleader.common.util.image.GcsLightweightClient;
import org.mockito.Mockito;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.ParameterizedTypeReference;
import org.wiremock.integrations.testcontainers.WireMockContainer;
import reactor.core.publisher.Flux;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Configuration
public class TestBeanConfiguration {

    @Bean
    public GreenMail greenMail(@Value("${spring.mail.port}") final Integer emailPort) {
        return new GreenMail(new ServerSetup(emailPort, null, ServerSetup.PROTOCOL_SMTP))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
    }

    @Bean
    public ChatModel chatModel() {
        return new StubChatModel();
    }

    @Bean
    public ChatClient chatClient() {
        // Check if running in native image mode
        boolean isNativeImage = System.getProperty("org.graalvm.nativeimage.imagecode") != null;

        if (isNativeImage) {
            // Use JDK proxy for native image (Mockito doesn't work)
            return (ChatClient) Proxy.newProxyInstance(
                    ChatClient.class.getClassLoader(),
                    new Class<?>[]{ChatClient.class},
                    new ChatClientInvocationHandler()
            );
        } else {
            // Use Mockito for JVM tests (allows Mockito.when() stubbing)
            return Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
        }
    }

    @Bean
    public WireMockContainer mockServer() {
        // Use WireMock in Docker to avoid native image resource loading issues
        var container = new WireMockContainer("wiremock/wiremock:3.10.0")
                .withMapping("test", IntegrationTest.class, "wiremock/mappings");
        container.start();
        return container;
    }

    @Bean
    @Primary
    public GcsLightweightClient mockGcsClient() {
        try {
            return new StubGcsClient();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create stub GCS client", e);
        }
    }

    private static class StubChatModel implements ChatModel {
        @Override
        public String call(String message) {
            return "Test AI response";
        }

        @Override
        public ChatResponse call(Prompt prompt) {
            var message = new AssistantMessage("Test AI response");
            var generation = new Generation(message);
            return new ChatResponse(List.of(generation));
        }

        @Override
        public ChatOptions getDefaultOptions() {
            return null;
        }
    }

    private static class ChatClientInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();

            // Handle prompt() methods - return ChatClientRequestSpec proxy
            if (methodName.equals("prompt")) {
                return Proxy.newProxyInstance(
                        ChatClient.ChatClientRequestSpec.class.getClassLoader(),
                        new Class<?>[]{ChatClient.ChatClientRequestSpec.class},
                        new RequestSpecInvocationHandler()
                );
            }

            // Default: return null
            return null;
        }
    }

    private static class RequestSpecInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();
            Class<?> returnType = method.getReturnType();

            // Handle call() method - return CallResponseSpec proxy
            if (methodName.equals("call")) {
                return Proxy.newProxyInstance(
                        ChatClient.CallResponseSpec.class.getClassLoader(),
                        new Class<?>[]{ChatClient.CallResponseSpec.class},
                        new CallResponseSpecInvocationHandler()
                );
            }

            // Handle stream() method - return StreamResponseSpec proxy
            if (methodName.equals("stream")) {
                return Proxy.newProxyInstance(
                        ChatClient.StreamResponseSpec.class.getClassLoader(),
                        new Class<?>[]{ChatClient.StreamResponseSpec.class},
                        new StreamResponseSpecInvocationHandler()
                );
            }

            // Builder pattern methods return this
            if (returnType.equals(ChatClient.ChatClientRequestSpec.class)) {
                return proxy;
            }

            return null;
        }
    }

    private static class CallResponseSpecInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();

            return switch (methodName) {
                case "content" -> "Test AI response";
                case "entity" -> null;  // Return null for entity calls
                case "chatResponse" -> {
                    var message = new AssistantMessage("Test AI response");
                    var generation = new Generation(message);
                    yield new ChatResponse(List.of(generation));
                }
                default -> null;
            };
        }
    }

    private static class StreamResponseSpecInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            String methodName = method.getName();

            return switch (methodName) {
                case "content" -> Flux.just("Test AI response");
                case "chatResponse" -> {
                    var message = new AssistantMessage("Test AI response");
                    var generation = new Generation(message);
                    yield Flux.just(new ChatResponse(List.of(generation)));
                }
                default -> Flux.empty();
            };
        }
    }

    private static class StubGcsClient extends GcsLightweightClient {
        private final byte[] testImageBytes = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
        );

        public StubGcsClient() throws Exception {
            super();
        }

        @Override
        public String uploadImage(byte[] image, String fileName, Map<String, String> metadata) {
            return "gs://ai-images-top-leader/test_image.png";
        }

        @Override
        public String uploadImage(byte[] image, String bucketName, String fileName, Map<String, String> metadata) {
            return "gs://ai-images-top-leader/test_image.png";
        }

        @Override
        public byte[] downloadImage(String gsUrl) {
            return testImageBytes;
        }

        @Override
        public byte[] downloadImage(String bucketName, String fileName) {
            return testImageBytes;
        }
    }
}
