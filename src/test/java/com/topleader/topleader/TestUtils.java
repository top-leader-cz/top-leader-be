/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import lombok.SneakyThrows;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.JsonNode;
import net.javacrumbs.jsonunit.core.Option;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;


/**
 * @author Daniel Slavik
 */
public final class TestUtils {

    private final static JsonMapper MAPPER_V3 = JsonMapper.builder().build();

    private TestUtils() {
        //util class
    }

    @SneakyThrows
    public static String readFileAsString(String name) {
        return new String(readFileAsBytes(name));
    }

    @SneakyThrows
    public static JsonNode readFileAsJson(String name) {
        var data = readFileAsString(name);
        return MAPPER_V3.readTree(data);
    }

    @SneakyThrows
    public static byte[] readFileAsBytes(String name) {
        return Files.readAllBytes(Paths.get(Objects.requireNonNull(TestUtils.class.getClassLoader()
            .getResource(name)).toURI()));
    }

    public static void assertJsonEquals(String result, String expected) {
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER, Option.IGNORING_EXTRA_FIELDS)
                .isEqualTo(expected);
    }
}
