package com.topleader.topleader.util.common;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.vavr.control.Try;
import org.springframework.core.ParameterizedTypeReference;


public final class JsonUtils {

    private JsonUtils() {}

    public static  ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    public static String toJsonString(Object data) {
        return Try.of(() -> MAPPER.writeValueAsString(data))
                .getOrElseThrow(e -> new  JsonConversionException("Error while parsing json!", e));
    }

    public static <T> T fromJson(String json, ParameterizedTypeReference<T> typeReference) {
        return Try.of(() -> MAPPER.readValue(json, new TypeReference<T>() {
                    @Override
                    public java.lang.reflect.Type getType() {
                        return typeReference.getType();
                    }
                }))
                .getOrElseThrow(e -> new JsonConversionException("Error while parsing json!", e));
    }

    public static JsonNode toJsonNode(String json) {
        return Try.of(() -> MAPPER.readTree(json))
                .getOrElseThrow(e -> new  JsonConversionException("Error while parsing json!", e));
    }


    public static <T> T fromJsonString(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static <T> T fromJsonString(String json, TypeReference<T> type) {
        try {
            return MAPPER.readValue(json, type);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
