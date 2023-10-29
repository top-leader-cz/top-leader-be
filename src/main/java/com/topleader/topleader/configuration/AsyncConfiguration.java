/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableAsync;


/**
 * @author Daniel Slavik
 */
@Profile("!test")
@Configuration
@EnableAsync
public class AsyncConfiguration {
}
