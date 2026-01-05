/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.exception;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author Daniel Slavik
 */
@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ApiValidationException extends RuntimeException {
    private final Error error;
    private final String message;

    public ApiValidationException(String code, String field, String value, String message) {
        this.error = new Error(code, List.of(new ErrorField(field, value)));
        this.message = message;
    }

    public record Error (String code, List<ErrorField> files) implements Serializable {}
    public record ErrorField(String name, String value) implements Serializable {}
}
