/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.util.user;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import static com.topleader.topleader.user.User.Authority.HR;


/**
 * @author Daniel Slavik
 */
public final class UserDetailUtils {

    private UserDetailUtils() {
        //util class
    }

    public static boolean isHr(final UserDetails user) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(HR.name()::equalsIgnoreCase);
    }
}
