package com.topleader.topleader.hr.domain;

import com.topleader.topleader.user.User;
import com.topleader.topleader.user.manager.Manager;

import java.util.Set;

public record UserDto(
        String username,
        String firstName,
        String lastName,
        String timeZone,
        User.Status status,
        Set<User.Authority> authorities,
        String position,
        String manager,
        boolean isManager
 ) {
    public static UserDto fromUser(User user) {
        return new UserDto(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getTimeZone(),
                user.getStatus(),
                user.getAuthorities(),
                user.getPosition(),
                user.getManagers().stream().findFirst().map(User::getUsername).orElse(null),
                user.getAuthorities().contains(User.Authority.MANAGER)
        );
     }
}

