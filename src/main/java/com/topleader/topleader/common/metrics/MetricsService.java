/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Centralized service for recording business metrics using Micrometer.
 * All counters and timers are created dynamically with automatic caching by MeterRegistry.
 */
@Service
public class MetricsService {

    private static final String METRIC_PREFIX = "topleader.";

    private final MeterRegistry registry;

    public MetricsService(MeterRegistry registry) {
        this.registry = registry;
    }

    // === Login Metrics ===

    public void incrementLogin() {
        counter("logins.total").increment();
    }

    public void incrementLogin(String provider) {
        counter("logins.total", "provider", provider).increment();
    }

    // === Session Metrics ===

    public void incrementSessionScheduled() {
        counter("sessions.scheduled.total").increment();
    }

    public void incrementSessionScheduled(String coachUsername) {
        counter("sessions.scheduled.total", "coach", coachUsername).increment();
    }

    public void incrementSessionCompleted() {
        counter("sessions.completed.total").increment();
    }

    public void incrementSessionCancelled() {
        counter("sessions.cancelled.total").increment();
    }

    public void recordSessionDuration(Duration duration) {
        timer("session.duration").record(duration);
    }

    // === Feedback Metrics ===

    public void incrementFeedbackSubmitted() {
        counter("feedback.submitted.total").increment();
    }

    public void recordFeedbackScore(double score) {
        summary("feedback.score").record(score);
    }

    // === Credit Metrics ===

    public void incrementCreditPurchased(int amount) {
        counter("credits.purchased.total").increment(amount);
    }

    public void incrementCreditConsumed() {
        counter("credits.consumed.total").increment();
    }

    // === User Metrics ===

    public void incrementUserCreated(String role) {
        counter("users.created.total", "role", role).increment();
    }

    // === API Metrics ===

    public void recordApiResponseTime(String endpoint, long millis) {
        timer("api.response.time", "endpoint", endpoint)
                .record(millis, TimeUnit.MILLISECONDS);
    }

    // === Helper Methods ===

    private Counter counter(String name, String... tags) {
        return Counter.builder(METRIC_PREFIX + name)
                .tags(tags)
                .register(registry);
    }

    private Timer timer(String name, String... tags) {
        return Timer.builder(METRIC_PREFIX + name)
                .tags(tags)
                .register(registry);
    }

    private DistributionSummary summary(String name, String... tags) {
        return DistributionSummary.builder(METRIC_PREFIX + name)
                .tags(tags)
                .register(registry);
    }
}
