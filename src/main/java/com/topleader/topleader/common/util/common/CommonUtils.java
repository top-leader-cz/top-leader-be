package com.topleader.topleader.common.util.common;


import jakarta.persistence.EntityNotFoundException;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class CommonUtils {

    public static final DateTimeFormatter TOP_LEADER_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:sa");

    public String generateToken() {
        return Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    }

    public void sleep(TimeUnit timeUnit, long time) {
        try {
            timeUnit.sleep(time);
        } catch (InterruptedException e) {
            log.warn("Failed to sleep", e);
            Thread.currentThread().interrupt();
        }
    }

    public Supplier<EntityNotFoundException> entityNotFound(String message) {
        return () -> new EntityNotFoundException(message);
    }

    /**
     * Try.of(() -> ...).getOrElseThrow(...)
     */
    public <T> T tryGet(Callable<T> supplier, Function<Exception, RuntimeException> exceptionMapper) {
        try {
            return supplier.call();
        } catch (Exception e) {
            throw exceptionMapper.apply(e);
        }
    }

    public <T> T tryGet(Callable<T> supplier) {
        return tryGet(supplier, RuntimeException::new);
    }

    /**
     * Try.of(() -> ...).onFailure(log::error).getOrElse(defaultValue)
     */
    public <T> T tryGetOrElse(Callable<T> supplier, T defaultValue, String errorMessage) {
        try {
            return supplier.call();
        } catch (Exception e) {
            log.error(errorMessage, e);
            return defaultValue;
        }
    }

    public <T> T tryGetOrElse(Callable<T> supplier, T defaultValue) {
        try {
            return supplier.call();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Try.of(() -> ...).map(mapper).onFailure(errorHandler).getOrElse(defaultValue)
     */
    public <T, R> R tryMapOrElse(Callable<T> supplier, Function<T, R> mapper, R defaultValue, Consumer<Exception> errorHandler) {
        try {
            return mapper.apply(supplier.call());
        } catch (Exception e) {
            errorHandler.accept(e);
            return defaultValue;
        }
    }

    /**
     * Try.of(() -> ...).onFailure(log::error).getOrNull()
     */
    public <T> T tryGetOrNull(Callable<T> supplier, String errorMessage) {
        return tryGetOrElse(supplier, null, errorMessage);
    }

    /**
     * Try.run(() -> ...).onFailure(log::error)
     */
    public void tryRun(ThrowingRunnable action, String errorMessage) {
        try {
            action.run();
        } catch (Exception e) {
            log.error(errorMessage, e);
        }
    }

    @FunctionalInterface
    public interface ThrowingRunnable {
        void run() throws Exception;
    }

}
