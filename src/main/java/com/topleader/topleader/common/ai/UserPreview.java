package com.topleader.topleader.common.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties
public class UserPreview {
    private String title;
    private String url;
    private String length;
    private String thumbnail;
}
