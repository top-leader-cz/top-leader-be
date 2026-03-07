package com.topleader.topleader.common.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginAttemptServiceTest {

    private LoginAttemptService service;

    @BeforeEach
    void setUp() {
        var properties = new RateLimitProperties();
        properties.setMaxAttempts(3);
        properties.setBlockDurationMinutes(15);
        service = new LoginAttemptService(properties);
    }

    @Test
    void shouldNotBlockBeforeMaxAttempts() {
        service.recordFailedAttempt("1.2.3.4", "user@test.com");
        service.recordFailedAttempt("1.2.3.4", "user@test.com");

        assertThat(service.isBlocked("1.2.3.4")).isFalse();
    }

    @Test
    void shouldBlockAfterMaxAttempts() {
        service.recordFailedAttempt("1.2.3.4", "user@test.com");
        service.recordFailedAttempt("1.2.3.4", "user@test.com");
        service.recordFailedAttempt("1.2.3.4", "user@test.com");

        assertThat(service.isBlocked("1.2.3.4")).isTrue();
    }

    @Test
    void shouldIsolateDifferentIps() {
        service.recordFailedAttempt("1.2.3.4", "user@test.com");
        service.recordFailedAttempt("1.2.3.4", "user@test.com");
        service.recordFailedAttempt("1.2.3.4", "user@test.com");

        assertThat(service.isBlocked("1.2.3.4")).isTrue();
        assertThat(service.isBlocked("5.6.7.8")).isFalse();
    }

    @Test
    void shouldResetAttempts() {
        service.recordFailedAttempt("1.2.3.4", "user@test.com");
        service.recordFailedAttempt("1.2.3.4", "user@test.com");
        service.recordFailedAttempt("1.2.3.4", "user@test.com");

        assertThat(service.isBlocked("1.2.3.4")).isTrue();

        service.resetAttempts("1.2.3.4");

        assertThat(service.isBlocked("1.2.3.4")).isFalse();
    }

    @Test
    void shouldNotBlockNewIp() {
        assertThat(service.isBlocked("10.0.0.1")).isFalse();
    }
}
