package com.topleader.topleader;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;


@Slf4j
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
