/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.util.common;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.List;
import java.util.Set;

/**
 * Wrapper for JSON string values stored as JSONB in PostgreSQL.
 * Spring Data JDBC will use registered converters for this type.
 */
public record JsonbValue(String json) {

    public static JsonbValue of(String json) {
        return new JsonbValue(json);
    }

    public static JsonbValue ofNull() {
        return new JsonbValue(null);
    }

    public static JsonbValue fromList(List<String> list) {
        return list != null ? of(JsonUtils.toJsonString(list)) : ofNull();
    }

    public static <T> JsonbValue fromSet(Set<T> set) {
        return set != null ? of(JsonUtils.toJsonString(set)) : ofNull();
    }

    public boolean isNull() {
        return json == null || json.isBlank();
    }

    // Static methods that handle null JsonbValue safely
    public static List<String> toStringList(JsonbValue value) {
        return value != null && !value.isNull()
                ? JsonUtils.fromJsonString(value.json(), new TypeReference<List<String>>() {})
                : List.of();
    }

    public static Set<String> toStringSet(JsonbValue value) {
        return value != null && !value.isNull()
                ? JsonUtils.fromJsonString(value.json(), new TypeReference<Set<String>>() {})
                : Set.of();
    }

    public static <T> Set<T> toSet(JsonbValue value, TypeReference<Set<T>> typeRef) {
        return value != null && !value.isNull()
                ? JsonUtils.fromJsonString(value.json(), typeRef)
                : Set.of();
    }
}
