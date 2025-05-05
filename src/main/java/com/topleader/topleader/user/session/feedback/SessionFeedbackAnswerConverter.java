package com.topleader.topleader.user.session.feedback;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import lombok.SneakyThrows;

import java.util.Map;

public class SessionFeedbackAnswerConverter implements AttributeConverter<Map<String, Integer>, String> {

    private static final ObjectMapper OB = new ObjectMapper();

    @Override
    @SneakyThrows
    public String convertToDatabaseColumn(Map<String, Integer> data) {
        if (data == null) {
            return null;
        }

        return OB.writeValueAsString(data);
    }

    @Override
    @SneakyThrows
    public Map<String, Integer> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Map.of();
        }

        return OB.readValue(dbData, new TypeReference<>() {
        });
    }
}
