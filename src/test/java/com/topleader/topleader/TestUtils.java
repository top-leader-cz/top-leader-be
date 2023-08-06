/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;


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
}
