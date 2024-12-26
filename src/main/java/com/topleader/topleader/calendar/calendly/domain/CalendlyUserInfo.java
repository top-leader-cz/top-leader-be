package com.topleader.topleader.calendar.calendly.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CalendlyUserInfo {

    private Resource resource;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Resource {
        private String email;
    }

}

