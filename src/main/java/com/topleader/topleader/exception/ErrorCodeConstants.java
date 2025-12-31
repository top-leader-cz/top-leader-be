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

    public static final String DIFFERENT_COACH_NOT_PERMITTED = "different.coach.not.permitted";

    public static final String FROM_ALREADY_SUBMITTED = "form.already.submitted";

    public static final String USER_NOT_FOUND = "user.not.found";
    public static final String UNABLE_TO_DELETE = "unable.to.delete";
    public static final String USER_NO_AUTHORIZED = "user.not.authorized";
    public static final String ALLOCATION_ALREADY_EXISTS = "allocation.already.exists";
    public static final String CAPACITY_EXCEEDED = "capacity.exceeded";
    public static final String PACKAGE_INACTIVE = "package.inactive";
    public static final String ALLOCATED_BELOW_CONSUMED = "allocation.below.consumed";
    public static final String NO_UNITS_AVAILABLE = "no.units.available";



}
