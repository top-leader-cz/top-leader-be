/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.topleader.topleader.configuration.AiTestSettings;
import com.topleader.topleader.configuration.EnablePostgresTestContainerContextCustomizerFactory;
import com.topleader.topleader.configuration.TestBeanConfiguration;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.ZoneOffset;
import java.util.TimeZone;
import java.util.function.Supplier;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


/**
 * @author Daniel Slavik
 */

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = {
    TopLeaderApplication.class,
    TestBeanConfiguration.class})
@TestExecutionListeners(mergeMode =
    TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
    listeners = {ResetDatabaseAfterTestMethodListener.class}
)
@EnablePostgresTestContainerContextCustomizerFactory.EnabledPostgresTestContainer
@DisabledInAotMode
public abstract class IntegrationTest {

    protected MockMvc mvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected GreenMail greenMail;

    @Autowired
    protected MockWebServer mockServer;

    private final AiTestSettings aiTests = new AiTestSettings();

    protected void stubResponse(String path, Supplier<MockResponse> responseSupplier) {
        aiTests.stubResponse(path, responseSupplier);
    }

    protected void stubAiResponse(String bodyContains, String responseContent) {
        aiTests.stubAiResponse(bodyContains, responseContent);
    }

    protected void stubAiToolCall(String bodyContains, String toolName, String toolArgs) {
        aiTests.stubAiToolCall(bodyContains, toolName, toolArgs);
    }

    @BeforeEach
    public void setUp() {
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
            .apply(springSecurity())
            .build();
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        aiTests.resetAndInstall(mockServer);

        if(!greenMail.isRunning()) {
            greenMail.start();
        }
    }

    @AfterEach
    public void testCleanUp() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

}
