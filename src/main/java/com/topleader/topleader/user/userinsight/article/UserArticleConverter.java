package com.topleader.topleader.user.userinsight.article;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.user.session.domain.UserArticle;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Converter(autoApply = true)
@RequiredArgsConstructor
public class UserArticleConverter implements AttributeConverter<UserArticle, String> {

    private final ObjectMapper objectMapper;

    @Override
    public String convertToDatabaseColumn(UserArticle userArticle) {
        if (userArticle == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(userArticle);
        } catch (JsonProcessingException e) {
            log.error("Error converting UserArticle to JSON", e);
            throw new RuntimeException("Error converting UserArticle to JSON", e);
        }
    }

    @Override
    public UserArticle convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(json, UserArticle.class);
        } catch (JsonProcessingException e) {
            log.error("Error converting JSON to UserArticle", e);
            throw new RuntimeException("Error converting JSON to UserArticle", e);
        }
    }
}