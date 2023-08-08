/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * @author Daniel Slavik
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ApiValidationException extends RuntimeException {
    private final String field;
    private final String message;
}
