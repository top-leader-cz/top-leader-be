package com.topleader.topleader.user.session.feedback;

import com.fasterxml.jackson.core.type.TypeReference;
import jakarta.persistence.AttributeConverter;
import lombok.SneakyThrows;

import java.util.Map;

import static com.topleader.topleader.util.common.JsonUtils.MAPPER;

public class SessionFeedbackAnswerConverter implements AttributeConverter<Map<String, Integer>, String> {


    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(Map<String, Integer> data) {
        if (data == null) {
            return null;
        }

        return MAPPER.writeValueAsString(data);
    }

    @Override
    @SneakyThrows
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Map.of();
        }

        return MAPPER.readValue(dbData, new TypeReference<>() {
        });
    }
}
