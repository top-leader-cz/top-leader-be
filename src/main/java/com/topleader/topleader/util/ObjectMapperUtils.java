/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;


/**
 * @author Daniel Slavik
 */
public final class ObjectMapperUtils {

    private static final ObjectMapper OB = new ObjectMapper()
        .registerModule(new JavaTimeModule())
        ;

    private ObjectMapperUtils() {
        //util class
    }

    public static <T> String toJsonString(T data) {
        try {
            return OB.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return OB.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
