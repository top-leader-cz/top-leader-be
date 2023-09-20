/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import com.topleader.topleader.configuration.EnablePostgresTestContainerContextCustomizerFactory;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


/**
 * @author Daniel Slavik
 */
@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestExecutionListeners(mergeMode =
    TestExecutionListeners.MergeMode.MERGE_WITH_DEFAULTS,
    listeners = {ResetDatabaseAfterTestMethodListener.class}
)
@EnablePostgresTestContainerContextCustomizerFactory.EnabledPostgresTestContainer
public abstract class IntegrationTest implements ApplicationContextAware {

    protected MockMvc mvc;

    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) applicationContext)
            .apply(springSecurity())
            .build();

    }

}
