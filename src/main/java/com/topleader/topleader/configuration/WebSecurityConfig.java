/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import com.topleader.topleader.util.user.UserDetailUtils;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.security.web.context.SecurityContextHolderFilter;


/**
 * @author Daniel Slavik
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String  USER_MDC_KEY = "username";


    @Bean
    public SecurityFilterChain googleSync(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/login/google")
            .authorizeHttpRequests(requests ->
                requests.anyRequest().permitAll()
            )
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
        ;
        return http.build();
    }

    @Bean
    public SecurityFilterChain protectedChain(HttpSecurity http, PasswordEncoder passwordEncoder) throws Exception {
        http
            .securityMatcher("/api/protected/**")
            .userDetailsService(new InMemoryUserDetailsManager(
                User.withUsername("job-trigger")
                    .passwordEncoder(passwordEncoder::encode)
                    .password("V7s7REyHo4v2HM9T")
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
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
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
                        "/v3/api-docs/**", "/api/latest/coaches/**").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(e ->
                e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            )
            .formLogin(f -> f
                .successHandler((req, res, auth) -> res.setStatus(HttpStatus.NO_CONTENT.value()))
                .failureHandler(new SimpleUrlAuthenticationFailureHandler())
            )
            .logout(l ->
                l.logoutSuccessHandler(new HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
            )
            .csrf(AbstractHttpConfigurer::disable)
            .cors(AbstractHttpConfigurer::disable)
        ;

          return http.build();
    }

//    @Bean
//    public SecurityFilterChain usernameInLogChain(HttpSecurity http) throws Exception {
//        return http.addFilterAfter((request, response, filterChain) -> {
//                    try {
//                        var username = UserDetailUtils.getLoggedUsername();
//                        MDC.put(USER_MDC_KEY, username);
//                        filterChain.doFilter(request, response);
//                    } finally {
//                        MDC.remove(USER_MDC_KEY);
//                    }
//                }, SecurityContextHolderFilter.class)
//            .build();
//    }

}
