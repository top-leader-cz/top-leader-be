/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;


import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;

import java.util.Set;

@Configuration
public class MetricsConfiguration {

    private static final Set<String> DENIED_PREFIXES = Set.of(
            "jvm.buffer", "jvm.classes", "jvm.info", "jvm.compilation",
            "system.cpu", "process.files", "disk.",
            "logback.", "tomcat.",
            "spring.data", "spring.security"
    );

    @Bean
    public MeterFilter denyUnusedMetrics() {
        return MeterFilter.deny(id ->
                DENIED_PREFIXES.stream().anyMatch(prefix -> id.getName().startsWith(prefix)));
    }

    @Bean
    public MeterFilter limitHttpUriCardinality() {
        return MeterFilter.maximumAllowableTags("http.server.requests", "uri", 50,
                MeterFilter.deny());
    }

    @Bean
    public ScheduledSessionMetrics scheduledSessionMetrics(
            MeterRegistry registry,
            ScheduledSessionRepository repository) {

        Gauge.builder("topleader.sessions.scheduled", repository,
                        CrudRepository::count)
                .description("Total number of scheduled sessions")
                .register(registry);

        for (ScheduledSession.Status status : ScheduledSession.Status.values()) {
            Gauge.builder("topleader.sessions.by_status", repository,
                            repo -> repo.countByStatus(status))
                    .tag("status", status.name().toLowerCase())
                    .description("Number of scheduled sessions by status")
                    .register(registry);
        }

        return new ScheduledSessionMetrics();
    }

    public static class ScheduledSessionMetrics {
    }
}
