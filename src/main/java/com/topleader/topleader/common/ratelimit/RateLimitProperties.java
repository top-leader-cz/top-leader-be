package com.topleader.topleader.common.ratelimit;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "top-leader.rate-limit.login")
public class RateLimitProperties {

    private int maxAttempts = 3;

    private int blockDurationMinutes = 15;
}
