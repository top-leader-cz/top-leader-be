/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.util.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Set;


/**
 * @author Daniel Slavik
 */
@Converter
public class SetConverter implements AttributeConverter<Set<String>, String> {



    private static final ObjectMapper OB = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<String> data) {
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
    public Set<String> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Set.of();
        }

        try {
            return OB.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
