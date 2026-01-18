/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.util.error;

import com.topleader.topleader.common.exception.ApiValidationException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


/**
 * @author Daniel Slavik
 */
@Slf4j
@RestController
@ControllerAdvice
@RequestMapping(produces = MediaType.APPLICATION_JSON_VALUE)
public class ErrorController {

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public List<ErrorDto> handleValidationExceptions(
        MethodArgumentNotValidException ex
    ) {
        return ex.getBindingResult().getAllErrors().stream()
            .map(e -> {
                String fieldName = ((FieldError) e).getField();
                return new ErrorDto(
                    e.getCode(),
                    List.of(new ErrorCodeFieldDto(fieldName, Objects.toString(((FieldError) e).getRejectedValue()))),
                    e.getDefaultMessage()
                );
            })
            .toList();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(value = ApiValidationException.class)
    public List<ErrorDto> handleApiValidationExceptions(
        ApiValidationException ex
    ) {
        return List.of(
            new ErrorDto(
                Optional.ofNullable(ex.getError()).map(ApiValidationException.Error::code).orElse(null),
                Optional.ofNullable(ex.getError()).map(ApiValidationException.Error::files).orElse(List.of()).stream()
                    .map(ErrorCodeFieldDto::from)
                    .toList(),
                ex.getMessage()
            )
        );
    }

    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(value = AccessDeniedException.class)
    public List<ErrorDto> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return List.of(
            new ErrorDto(
                "ACCESS_DENIED",
                List.of(),
                "You do not have permission to access this resource"
            )
        );
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = Exception.class)
    public List<ErrorDto> handleGenericException(Exception ex) {
        log.error("Internal server error", ex);
        return List.of(
            new ErrorDto(
                "INTERNAL_ERROR",
                List.of(),
                ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred"
            )
        );
    }

    public record ErrorDto(String errorCode, List<ErrorCodeFieldDto> fields, String errorMessage) {
    }

    public record ErrorCodeFieldDto(String name, String value) {
        public static ErrorCodeFieldDto from(ApiValidationException.ErrorField f) {
            return new ErrorCodeFieldDto(f.name(), f.value());
        }
    }
}
