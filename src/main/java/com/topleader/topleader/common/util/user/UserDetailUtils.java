/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.util.user;

import com.topleader.topleader.user.User;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;

import static com.topleader.topleader.user.User.Authority.ADMIN;
import static com.topleader.topleader.user.User.Authority.HR;


/**
 * @author Daniel Slavik
 */
@UtilityClass
public final class UserDetailUtils {


    public static boolean isHr(final UserDetails user) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(HR.name()::equalsIgnoreCase);
    }

    public static boolean isAdmin(final UserDetails user) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(ADMIN.name()::equalsIgnoreCase);
    }

    public static boolean sendInvite(User.Status oldStatus, User.Status newStatus) {
        return User.Status.PENDING == oldStatus && (User.Status.AUTHORIZED == newStatus || User.Status.PAID == newStatus);
    }

    public static String getLoggedUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(principal -> (UserDetails) principal)
                .map(UserDetails::getUsername)
                .orElse("anonymous");
    }
}
