package com.topleader.topleader.common.calendar.calendly.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class AccessTokenRequest {

    @JsonProperty("grant_type")
    String grantType;

    String code;

    @JsonProperty("redirect_uri")
    String redirectUri;

}
