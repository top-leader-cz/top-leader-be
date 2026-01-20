package com.topleader.topleader;

import java.time.ZoneOffset;
import java.util.TimeZone;

import com.topleader.topleader.common.calendar.calendly.CalendlyProperties;
import com.topleader.topleader.common.upload.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.data.web.config.EnableSpringDataWebSupport;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@EnableConfigurationProperties({CalendlyProperties.class, UploadProperties.class})
@EnableMethodSecurity(
    securedEnabled = true,
    jsr250Enabled = true)
@RestController
@SpringBootApplication
@RequestMapping("/")
@EnableSpringDataWebSupport
public class TopLeaderApplication {

    public static void main(String[] args) {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC));
        SpringApplication.run(TopLeaderApplication.class, args);
    }

    @GetMapping("_ah/start")
    public String legacyHealthCheck() {
        return "App ok";
    }

    @GetMapping("_ah/warmup")
    public String legacyWarm() {
        return "App ok";
    }





}
