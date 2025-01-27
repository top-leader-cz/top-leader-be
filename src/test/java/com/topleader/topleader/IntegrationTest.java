/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.GreenMail;
import com.topleader.topleader.configuration.EnablePostgresTestContainerContextCustomizerFactory;
import org.jetbrains.annotations.NotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.ZoneOffset;
import java.util.TimeZone;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


/**
 * @author Daniel Slavik
 */

@ActiveProfiles("test")
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(mergeMode =
    TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
    listeners = {ResetDatabaseAfterTestMethodListener.class}
)
@EnablePostgresTestContainerContextCustomizerFactory.EnabledPostgresTestContainer
public abstract class IntegrationTest implements ApplicationContextAware {

    protected MockMvc mvc;

    @Autowired
    protected GreenMail greenMail;

    @Autowired
    protected WireMockServer mockServer;

    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) applicationContext)
            .apply(springSecurity())
            .build();

    }

    @BeforeEach
    public void setUp() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        if(!mockServer.isRunning()) {
            mockServer.start();
        }

        if(!greenMail.isRunning()) {
            greenMail.start();
        }
    }

    @AfterEach
    public void testCleanUp() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();

    }


}
