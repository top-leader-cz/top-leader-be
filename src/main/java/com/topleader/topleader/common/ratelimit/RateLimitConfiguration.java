package com.topleader.topleader.common.ratelimit;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
public class RateLimitConfiguration {

    @Bean
    public FilterRegistrationBean<LoginRateLimitFilter> loginRateLimitFilterRegistration(LoginAttemptService loginAttemptService) {
        var registration = new FilterRegistrationBean<>(new LoginRateLimitFilter(loginAttemptService));
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        registration.addUrlPatterns("/login");
        return registration;
    }
}
