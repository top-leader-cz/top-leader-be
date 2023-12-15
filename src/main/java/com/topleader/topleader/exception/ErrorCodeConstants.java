/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.exception;

/**
 * @author Daniel Slavik
 */
public final class ErrorCodeConstants {
    private ErrorCodeConstants() {
        //util class
    }

    public static final String FIELD_OUTSIDE_OF_FRAME = "field.outside.of.frame";
    public static final String MORE_THEN_24_EVENT = "event.longer.that.24";

    public static final String SESSION_IN_PAST = "session.in.past";
    public static final String TIME_NOT_AVAILABLE = "time.not.available";

    public static final String NOT_ENOUGH_CREDITS = "not.enough.credits";

    public static final String EMAIL_USED = "email.used";
    public static final String NOT_PART_OF_COMPANY = "not.part.of.company";

    public static final String INVALID_PASSWORD = "invalid.password";

    public static final String ALREADY_EXISTING = "already.existing";

    public static final String NOT_EMPTY = "field.cannot.be.empty";



}
