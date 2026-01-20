package com.topleader.topleader.configuration;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.topleader.topleader.common.util.image.GcsLightweightClient;
import com.topleader.topleader.config.stub.AiStubRegistry;
import com.topleader.topleader.config.stub.StubChatClient;
import com.topleader.topleader.config.stub.StubChatModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Base64;
import java.util.Map;

@Configuration
public class TestBeanConfiguration {

    @Bean
    public AiStubRegistry aiStubRegistry() {
        return new AiStubRegistry();
    }

    @Bean
    public GreenMail greenMail(@Value("${spring.mail.port}") final Integer emailPort) {
        return new GreenMail(new ServerSetup(emailPort, null, ServerSetup.PROTOCOL_SMTP))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
    }

    @Bean
    @Primary
    public ChatModel chatModel(AiStubRegistry registry) {
        return new StubChatModel(registry);
    }

    @Bean
    @Primary
    public ChatClient chatClient(AiStubRegistry registry) {
        return new StubChatClient(registry);
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
