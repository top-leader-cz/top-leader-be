package com.topleader.topleader.user.session.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserArticle {

    private String title;
    private String author;
    private String source;
    private String url;
    private String readTime;
    private String language;
    private String perex;
    private String summaryText;
    private String application;
    private String imagePrompt;
    private byte[] imageData;

}