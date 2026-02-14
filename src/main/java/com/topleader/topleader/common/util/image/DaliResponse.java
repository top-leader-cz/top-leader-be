package com.topleader.topleader.common.util.image;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record DaliResponse(
    Long created,
    List<ImageData> data
) {
    public record ImageData(
            @JsonProperty("revised_prompt")
        String revisedPrompt,
        String url
    ) {}
}
