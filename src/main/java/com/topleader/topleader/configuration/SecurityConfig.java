/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.configuration;

import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;


/**
 * @author Daniel Slavik
 */
@Configuration
public class SecurityConfig {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.jdbcAuthentication()
            .passwordEncoder(passwordEncoder)
            .dataSource(dataSource)
            .usersByUsernameQuery("select username, password, enabled from users where username = ?")
            .authoritiesByUsernameQuery("select username, authority from authorities where username = ?")
            ;
    }
}
