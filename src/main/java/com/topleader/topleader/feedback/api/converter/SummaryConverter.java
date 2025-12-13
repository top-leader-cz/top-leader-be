package com.topleader.topleader.feedback.api.converter;

import com.topleader.topleader.feedback.api.Summary;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.SneakyThrows;

import static com.topleader.topleader.util.common.JsonUtils.MAPPER;


@Converter
public class SummaryConverter implements AttributeConverter<Summary, String> {

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
