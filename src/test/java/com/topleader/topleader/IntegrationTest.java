/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import java.security.Principal;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;


/**
 * @author Daniel Slavik
 */
@Testcontainers
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTest implements ApplicationContextAware {


    @Container
    @ServiceConnection
    public static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    protected MockMvc mvc;

    public void setApplicationContext(@NotNull ApplicationContext applicationContext) {
        mvc = MockMvcBuilders.webAppContextSetup((WebApplicationContext) applicationContext)
            .apply(springSecurity())
            .build();
        //SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken("user", "pass"));

    }

}
