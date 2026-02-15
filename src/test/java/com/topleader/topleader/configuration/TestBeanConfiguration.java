package com.topleader.topleader.configuration;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.topleader.topleader.common.ai.McpToolsConfig;
import com.topleader.topleader.common.util.image.GcsLightweightClient;
import okhttp3.mockwebserver.MockWebServer;
import org.mockito.Mockito;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.function.Function;

@Configuration
public class TestBeanConfiguration {

    @Bean
    public GreenMail greenMail(@Value("${spring.mail.port}") final Integer emailPort) {
        return new GreenMail(new ServerSetup(emailPort, null, ServerSetup.PROTOCOL_SMTP))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
    }

    @SuppressWarnings("unchecked")
    @Bean
    @Primary
    @Qualifier("searchArticles")
    public Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> mockSearchArticles() {
        var mock = (Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>>)
                Mockito.mock(Function.class);
        Mockito.when(mock.apply(Mockito.any())).thenReturn(List.of());
        return mock;
    }

    @SuppressWarnings("unchecked")
    @Bean
    @Primary
    @Qualifier("searchVideos")
    public Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> mockSearchVideos() {
        var mock = (Function<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>>)
                Mockito.mock(Function.class);
        Mockito.when(mock.apply(Mockito.any())).thenReturn(List.of());
        return mock;
    }

    @Bean(destroyMethod = "close")
    public MockWebServer mockServer() throws IOException {
        var server = new MockWebServer();
        server.start(8060);
        return server;
    }

    @Bean
    @Primary
    public GcsLightweightClient mockGcsClient() {
        byte[] testImageBytes = Base64.getDecoder().decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8/5+hHgAHggJ/PchI7wAAAABJRU5ErkJggg=="
        );

        return new GcsLightweightClient(null) {
            @Override
            public String uploadImage(byte[] image, String fileName) {
                return "gs://ai-images-top-leader/test_image.png";
            }

            @Override
            public String uploadImage(byte[] image, String bucketName, String fileName) {
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

            @Override
            public List<String> listObjects(String bucketName) {
                return List.of();
            }

            @Override
            public List<String> listObjects() {
                return List.of();
            }
        };
    }

}
