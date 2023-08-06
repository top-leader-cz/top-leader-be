/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user;

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
public class RoleConverter implements AttributeConverter<Set<User.Authority>, String> {



    private static final ObjectMapper OB = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Set<User.Authority> data) {
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
    public Set<User.Authority> convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return Set.of(User.Authority.USER);
        }

        try {
            return OB.readValue(dbData, new TypeReference<>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
