package com.topleader.topleader.common.util.common;


import io.vavr.control.Try;
import jakarta.persistence.EntityNotFoundException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.action.internal.EntityActionVetoException;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class CommonUtils {

    public static final DateTimeFormatter TOP_LEADER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:sa");

    public String generateToken() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    public void sleep(TimeUnit timeUnit, long time) {
        Try.run(() -> timeUnit.sleep(time))
            .onFailure(e -> log.warn("Failed to sleep", e));
    }

    public Supplier<EntityNotFoundException> entityNotFound(String message) {
        return () -> new EntityNotFoundException(message);
    }


}
