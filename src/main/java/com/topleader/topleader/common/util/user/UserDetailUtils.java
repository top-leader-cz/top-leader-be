package com.topleader.topleader.common.util.user;

import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Optional;


/**
 * @author Daniel Slavik
 */
@UtilityClass
public final class UserDetailUtils {


    public static boolean isHr(final UserDetails user) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("HR"::equalsIgnoreCase);
    }

    public static boolean isAdmin(final UserDetails user) {
        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch("ADMIN"::equalsIgnoreCase);
    }

    public static String getLoggedUsername() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Authentication::getPrincipal)
                .map(principal -> (UserDetails) principal)
                .map(UserDetails::getUsername)
                .orElse("anonymous");
    }
}
