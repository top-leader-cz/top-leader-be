package com.topleader.topleader.user.userinsight.article;


import com.topleader.topleader.user.session.domain.UserArticle;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Converter(autoApply = true)
@RequiredArgsConstructor
public class UserArticleConverter implements AttributeConverter<UserArticle, String> {

    private final JsonMapper jsonMapper;

    @Override
    public String convertToDatabaseColumn(UserArticle userArticle) {
        if (userArticle == null) {
            return null;
        }

        return jsonMapper.writeValueAsString(userArticle);

    }

    @Override
    public UserArticle convertToEntityAttribute(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }

        return jsonMapper.readValue(json, UserArticle.class);

    }
}