package com.topleader.topleader.feedback.api.converter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.feedback.api.Summary;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;


@Converter
public class SummaryConverter implements AttributeConverter<Summary, String> {
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @SneakyThrows
    @Override
    public String convertToDatabaseColumn(Summary attribute) {
        return attribute== null ? null : MAPPER.writeValueAsString(attribute);
    }

    @SneakyThrows
    @Override
    public Summary convertToEntityAttribute(String dbData) {
        return dbData == null ?  null : MAPPER.readValue(dbData, Summary.class);
    }
}
