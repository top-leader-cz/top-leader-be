package com.topleader.topleader.user.session.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Accessors(chain = true)
@JsonPropertyOrder({"url", "perex", "title", "author", "source", "language", "readTime", "imageData", "application", "imagePrompt", "summaryText", "id"})
public class UserArticle {

    private Long id;
    private String title;
    private String originalTitle;
    private String author;
    private String source;
    private String url;
    private String readTime;
    private String language;
    private String sourceLanguage;
    private String perex;
    private String summaryText;
    private String application;
    private String imagePrompt;
    private String imageData;
    private String imageUrl;
    private String date;
    private List<String> keyTakeaways;
    private Integer relevanceScore;

}