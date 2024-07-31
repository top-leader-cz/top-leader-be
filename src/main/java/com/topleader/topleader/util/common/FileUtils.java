package com.topleader.topleader.util.common;

import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

@UtilityClass
public class FileUtils {

    @SneakyThrows
    public String loadFileAsString(String path) {
        try(var configInputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(path)) {
           return new String(configInputStream.readAllBytes());
        }
    }
}
