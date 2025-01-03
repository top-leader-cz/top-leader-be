package com.topleader.topleader.calendar.calendly;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.stringtemplate.v4.ST;

@Data
@ConfigurationProperties(prefix = "top-leader.calendly")
public class CalendlyProperties {

    private String clientId;
    private String clientSecrets;
    private String redirectUri;
    private String baseApiUrl;
    private String baseAuthUrl;
}
