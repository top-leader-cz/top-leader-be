package com.topleader.topleader.util.common;


import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.collection.List;
import io.vavr.control.Try;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@UtilityClass
@Slf4j
public class CommonUtils {

    public static final DateTimeFormatter TOP_LEADER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:sa");

    public String generateToken() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    public void sleep(TimeUnit timeUnit, long time) {
        Try.run(() -> timeUnit.sleep(time))
            .onFailure(e -> log.warn("Failed to sleep", e));

    }


}
