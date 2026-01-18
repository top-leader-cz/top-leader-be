package com.topleader.topleader.hr.domain;

import com.topleader.topleader.user.User;

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
        boolean isManager,
        String aspiredCompetency
 ) {
    public static UserDto fromUser(User user, String managerUsername) {
        return new UserDto(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getTimeZone(),
                user.getStatus(),
                user.getAuthorities(),
                user.getPosition(),
                managerUsername,
                user.getAuthorities().contains(User.Authority.MANAGER),
                user.getAspiredCompetency()
        );
     }
}
