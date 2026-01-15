/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import com.topleader.topleader.common.util.user.UserDetailUtils;
import io.micrometer.core.instrument.Counter;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;


/**
 * @author Daniel Slavik
 */
@Configuration
@EnableWebSecurity
@EnableJdbcHttpSession
public class WebSecurityConfig {

    private static final String  USER_MDC_KEY = "username";

    @Value("${top-leader.job-trigger.password}")
    private String jobTriggerPassword;


    @Bean
    @Order(1)
    public SecurityFilterChain googleSync(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/login/google", "/login/calendly")
            .authorizeHttpRequests(requests ->
                requests.anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
        ;
        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain protectedChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        http
            .securityMatcher("/api/protected/**")
            .userDetailsService(new InMemoryUserDetailsManager(
                User.withUsername("job-trigger")
                    .passwordEncoder(passwordEncoder::encode)
                    .password(jobTriggerPassword)
                    .authorities("JOB")
                    .build()
            ))
            .authorizeHttpRequests((requests) ->
                requests.anyRequest().authenticated()
            )
            .exceptionHandling(e ->
                e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .httpBasic(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
        ;
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain filterChain(HttpSecurity http, Counter loginCounter) throws Exception {
        http.addFilterAfter((request, response, filterChain) -> {
            try {
                var username = UserDetailUtils.getLoggedUsername();
                MDC.put(USER_MDC_KEY, username);
                filterChain.doFilter(request, response);
            } finally {
                MDC.remove(USER_MDC_KEY);
            }
        }, SecurityContextHolderFilter.class);

        http
            .authorizeHttpRequests((requests) -> requests
                .requestMatchers("/_ah/start", "/api/public/**", "/login", "/swagger-ui/**",
                        "/v3/api-docs/**", "/_ah/start","/_ah/warmup", "/actuator/health").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                })
            )
            .formLogin(f -> f
                .successHandler((req, res, auth) -> {
                    loginCounter.increment();
                    res.setStatus(HttpStatus.NO_CONTENT.value());
                })
                .failureHandler(new SimpleUrlAuthenticationFailureHandler())
            )
            .logout(l ->
                l.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            )
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:")
                )
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)
                .xssProtection(xss -> xss.headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
            )
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
        ;

          return http.build();
    }

}
