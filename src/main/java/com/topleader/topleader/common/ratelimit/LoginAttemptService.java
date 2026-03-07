package com.topleader.topleader.common.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class LoginAttemptService {

    private record Attempt(int count, Instant firstAttempt) {}

    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();
    private final int maxAttempts;
    private final Duration blockDuration;

    public LoginAttemptService(RateLimitProperties properties) {
        this.maxAttempts = properties.getMaxAttempts();
        this.blockDuration = Duration.ofMinutes(properties.getBlockDurationMinutes());
    }

    public void recordFailedAttempt(String ip, String username) {
        var now = Instant.now();
        var attempt = attempts.compute(ip, (key, existing) ->
                existing == null || isExpired(existing, now)
                        ? new Attempt(1, now)
                        : new Attempt(existing.count() + 1, existing.firstAttempt()));
        log.warn("Failed login attempt [{}/{}] from IP: {} for user: {}", attempt.count(), maxAttempts, ip, username);
    }

    public boolean isBlocked(String ip) {
        var attempt = attempts.get(ip);
        if (attempt == null) {
            return false;
        }
        if (isExpired(attempt, Instant.now())) {
            attempts.remove(ip);
            return false;
        }
        return attempt.count() >= maxAttempts;
    }

    public void resetAttempts(String ip) {
        attempts.remove(ip);
    }

    private boolean isExpired(Attempt attempt, Instant now) {
        return Duration.between(attempt.firstAttempt(), now).compareTo(blockDuration) > 0;
    }
}
