package com.topleader.topleader;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true)
@RestController
@SpringBootApplication
public class TopLeaderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TopLeaderApplication.class, args);
    }

    @GetMapping("/_ah/start")
    public String legacyHealthCheck() {
        return "App ok";
    }

}
