/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.history;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.topleader.topleader.history.data.StoredData;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;


/**
 * @author Daniel Slavik
 */
@Converter
public class HistoryDataConverter implements AttributeConverter<StoredData, String> {

    private static final ObjectMapper OB = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        ;

    @Override
    public String convertToDatabaseColumn(StoredData data) {
        if (data == null) {
            return null;
        }

        try {
            return OB.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public StoredData convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }

        try {
            return OB.readValue(dbData, StoredData.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}

