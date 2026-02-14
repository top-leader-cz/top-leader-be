package com.topleader.topleader.configuration;

import com.icegreen.greenmail.configuration.GreenMailConfiguration;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import com.topleader.topleader.ResetDatabaseAfterTestMethodListener;
import com.topleader.topleader.StubFunction;
import com.topleader.topleader.common.ai.McpToolsConfig;
import com.topleader.topleader.common.util.image.GcsLightweightClient;
import okhttp3.mockwebserver.MockWebServer;

import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Primary;

import com.topleader.topleader.common.email.Emailing;
import com.topleader.topleader.feedback.repository.RecipientRepository;
import com.topleader.topleader.message.MessageRepository;
import com.topleader.topleader.user.UserRepository;

import java.io.IOException;
import java.util.Base64;
import java.util.List;

@Configuration
@ImportRuntimeHints(TestBeanConfiguration.TestRuntimeHints.class)
public class TestBeanConfiguration {

    static class TestRuntimeHints implements RuntimeHintsRegistrar {
        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            // JDK proxies used as test doubles in unit tests (FeedbackControllerTest, MessageServiceTest)
            hints.proxies().registerJdkProxy(RecipientRepository.class);
            hints.proxies().registerJdkProxy(UserRepository.class);
            hints.proxies().registerJdkProxy(MessageRepository.class);
            hints.proxies().registerJdkProxy(Emailing.class);

            // SqlScriptsTestExecutionListener reflectively calls getDataSource()
            hints.reflection().registerTypeIfPresent(classLoader,
                    "org.springframework.jdbc.datasource.DataSourceTransactionManager",
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            // ResetDatabaseAfterTestMethodListener needs reflection for instantiation in native image
            hints.reflection().registerType(ResetDatabaseAfterTestMethodListener.class,
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
                    MemberCategory.INVOKE_PUBLIC_METHODS);

            // MessageService needs field access for ReflectionTestUtils.setField in MessageServiceTest
            hints.reflection().registerTypeIfPresent(classLoader,
                    "com.topleader.topleader.message.MessageService",
                    MemberCategory.DECLARED_FIELDS);

            // JsonPath function classes used by json-unit-assertj ($.length(), etc.)
            hints.reflection().registerTypeIfPresent(classLoader,
                    "com.jayway.jsonpath.internal.function.text.Length",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerTypeIfPresent(classLoader,
                    "com.jayway.jsonpath.internal.function.text.Concatenate",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerTypeIfPresent(classLoader,
                    "com.jayway.jsonpath.internal.function.numeric.Min",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerTypeIfPresent(classLoader,
                    "com.jayway.jsonpath.internal.function.numeric.Max",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerTypeIfPresent(classLoader,
                    "com.jayway.jsonpath.internal.function.numeric.Average",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);
            hints.reflection().registerTypeIfPresent(classLoader,
                    "com.jayway.jsonpath.internal.function.numeric.Sum",
                    MemberCategory.INVOKE_DECLARED_CONSTRUCTORS);

            // Test resource files read via TestUtils.readFileAsString()
            hints.resources().registerPattern("ai/**");
            hints.resources().registerPattern("sql/**");
            hints.resources().registerPattern("json/**");
            hints.resources().registerPattern("admin/**");
            hints.resources().registerPattern("availability/**");
            hints.resources().registerPattern("feedback/**");
            hints.resources().registerPattern("session/**");
            hints.resources().registerPattern("translation/**");
            hints.resources().registerPattern("user_insight/**");
            hints.resources().registerPattern("user_session/**");
            hints.resources().registerPattern("user_settings/**");
        }
    }

    @Bean
    public GreenMail greenMail(@Value("${spring.mail.port}") final Integer emailPort) {
        return new GreenMail(new ServerSetup(emailPort, null, ServerSetup.PROTOCOL_SMTP))
                .withConfiguration(GreenMailConfiguration.aConfig().withDisabledAuthentication());
    }

    @Bean
    @Primary
    @Qualifier("searchArticles")
    public StubFunction<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> mockSearchArticles() {
        return new StubFunction<>(request -> List.of());
    }

    @Bean
    @Primary
    @Qualifier("searchVideos")
    public StubFunction<McpToolsConfig.TavilySearchRequest, List<McpToolsConfig.TavilySearchResult>> mockSearchVideos() {
        return new StubFunction<>(request -> List.of());
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
