/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.topleader.topleader.configuration.TestBeanConfiguration;
import com.topleader.topleader.config.stub.AiStubRegistry;
import com.topleader.topleader.configuration.EnablePostgresTestContainerContextCustomizerFactory;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.wiremock.integrations.testcontainers.WireMockContainer;

import java.time.ZoneOffset;
import java.util.TimeZone;

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
public abstract class IntegrationTest implements ApplicationContextAware {

    // Static WireMock container shared across all tests
    protected static final WireMockContainer wireMockContainer;

    static {
        wireMockContainer = new WireMockContainer("wiremock/wiremock:3.10.0");
        wireMockContainer.start();
    }

    @DynamicPropertySource
    static void configureWireMockProperties(DynamicPropertyRegistry registry) {
        // Use getMappedPort(8080) for reliable port mapping - WireMock container runs on port 8080 internally
        var baseUrl = "http://" + wireMockContainer.getHost() + ":" + wireMockContainer.getMappedPort(8080);
        registry.add("openai.image.url", () -> baseUrl + "/image");
        registry.add("thumbmail", () -> baseUrl + "/hqdefault");
        registry.add("top-leader.calendly.base-api-url", () -> baseUrl);
        registry.add("top-leader.calendly.base-auth-url", () -> baseUrl);
    }

    protected MockMvc mvc;

    @Autowired
    protected GreenMail greenMail;

    @Autowired
    protected AiStubRegistry aiStubRegistry;

    /**
     * WireMock client configured to connect to the WireMockContainer.
     * @deprecated Use static WireMock.stubFor() instead for proper stub registration.
     */
    @Deprecated
    protected WireMock mockServer;

    /**
     * Get the base URL for the WireMock server.
     * Use this when constructing URLs that should be validated against WireMock.
     */
    protected String getWireMockBaseUrl() {
        return "http://" + wireMockContainer.getHost() + ":" + wireMockContainer.getMappedPort(8080);
    }

    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) applicationContext)
            .apply(springSecurity())
            .build();
    }

    @BeforeEach
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));

        // Reset AI stubs before each test
        aiStubRegistry.reset();

        // Configure WireMock - use static API with configureFor() for proper stub registration
        int port = wireMockContainer.getMappedPort(8080);
        String host = wireMockContainer.getHost();
        WireMock.configureFor(host, port);
        WireMock.reset();

        // Keep mockServer for backward compatibility (deprecated)
        mockServer = new WireMock(host, port);

        if (!greenMail.isRunning()) {
            greenMail.start();
        }
    }

    @AfterEach
    public void testCleanUp() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();

    }


}
