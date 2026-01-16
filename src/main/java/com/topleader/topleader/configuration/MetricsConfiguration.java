/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;


import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;

/**
 * Custom business metrics for monitoring.
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public Counter loginCounter(MeterRegistry registry) {
        return Counter.builder("topleader.logins.total")
                .description("Total number of successful user logins")
                .register(registry);
    }

    @Bean
    public Counter sessionScheduledCounter(MeterRegistry registry) {
        return Counter.builder("topleader.sessions.scheduled.total")
                .description("Total number of scheduled coaching sessions")
                .register(registry);
    }

    @Bean
    public ScheduledSessionMetrics scheduledSessionMetrics(
            MeterRegistry registry,
            ScheduledSessionRepository repository) {

        // Gauge for upcoming sessions
        Gauge.builder("topleader.sessions.scheduled", repository,
                        CrudRepository::count)
                .description("Total number of scheduled sessions")
                .register(registry);

        // Gauge for sessions by status
        for (ScheduledSession.Status status : ScheduledSession.Status.values()) {
            Gauge.builder("topleader.sessions.by_status", repository,
                            repo -> repo.count((root, query, cb) ->
                                    cb.equal(root.get("status"), status)))
                    .tag("status", status.name().toLowerCase())
                    .description("Number of scheduled sessions by status")
                    .register(registry);
        }

        return new ScheduledSessionMetrics();
    }

    public static class ScheduledSessionMetrics {
        // Marker class for bean creation
    }
}
