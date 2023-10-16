/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import net.javacrumbs.jsonunit.core.Option;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;


/**
 * @author Daniel Slavik
 */
public final class TestUtils {
    private TestUtils() {
        //util class
    }

    public static String readFileAsString(String name) throws URISyntaxException, IOException {
        return new String(readFileAsBytes(name));
    }

    public static byte[] readFileAsBytes(String name) throws URISyntaxException, IOException {
        return Files.readAllBytes(Paths.get(Objects.requireNonNull(TestUtils.class.getClassLoader()
            .getResource(name)).toURI()));
    }

    public static void assertJsonEquals(String result, String expected) {
        assertThatJson(result)
                .when(Option.IGNORING_ARRAY_ORDER)
                .isEqualTo(expected);
    }
}
