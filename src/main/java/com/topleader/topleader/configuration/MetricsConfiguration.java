/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;


import com.topleader.topleader.session.scheduled_session.ScheduledSession;
import com.topleader.topleader.session.scheduled_session.ScheduledSessionRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.repository.CrudRepository;

/**
 * Gauge metrics configuration for monitoring live repository state.
 * For counters and timers, use MetricsService instead.
 */
@Configuration
public class MetricsConfiguration {

    @Bean
    public ScheduledSessionMetrics scheduledSessionMetrics(
            MeterRegistry registry,
            ScheduledSessionRepository repository) {

        // Gauge for total scheduled sessions (live count)
        Gauge.builder("topleader.sessions.scheduled", repository,
                        CrudRepository::count)
                .description("Total number of scheduled sessions")
                .register(registry);

        // Gauge for sessions by status (with tags)
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
        // Marker class for bean creation
    }
}
