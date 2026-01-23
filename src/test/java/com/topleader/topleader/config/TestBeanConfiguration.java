package com.topleader.topleader.config;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.topleader.topleader.common.util.image.GcsLightweightClient;
import org.mockito.Mockito;


import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;

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
    public GreenMail greenMail(@Value("${spring.mail.port}") final Integer emailPort) {
        return new GreenMail(new ServerSetup(emailPort, null, ServerSetup.PROTOCOL_SMTP))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
    }

    @Bean
    public ChatModel chatModel() {
         return Mockito.mock(ChatModel.class);
    }

    @Bean
    public ChatClient chatClient() {
        return Mockito.mock(ChatClient.class, Mockito.RETURNS_DEEP_STUBS);
    }

    @Bean
    public WireMockServer mockServer() {
        return new WireMockServer(8060);
    }

    @Bean
    @Primary
    public GcsLightweightClient mockGcsClient() {
        GcsLightweightClient client = Mockito.mock(GcsLightweightClient.class);

        // Create a simple 1x1 pixel PNG image in bytes
        byte[] testImageBytes = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
        );

        try {
            // Mock upload to return a test URL
            Mockito.when(client.uploadImage(Mockito.any(byte[].class), Mockito.anyString(), Mockito.any(Map.class)))
                    .thenReturn("gs://ai-images-top-leader/test_image.png");

            // Mock download to return test image bytes
            Mockito.when(client.downloadImage("gs://ai-images-top-leader/test_image.png"))
                    .thenReturn(testImageBytes);

            // Mock download for any GCS URL
            Mockito.when(client.downloadImage(Mockito.anyString()))
                    .thenReturn(testImageBytes);

        } catch (Exception e) {
            throw new RuntimeException("Failed to setup mock GCS client", e);
        }

        return client;
    }

}
