package com.topleader.topleader.hr.domain;

import com.topleader.topleader.user.User;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record AddUserRequest(
        @NotEmpty
        String firstName,

        @NotEmpty
        String lastName,

        @NotEmpty
        Set<User.Authority> authorities,

        @NotEmpty
        @Email
        String username,

        @NotEmpty
        String timeZone,

        @NotNull
        User.Status status,

        @Pattern(regexp = "[a-z]{2}")
        String locale,
        String manager,

        boolean isManager,

        String position,

        String aspiredCompetency

) implements UserRequest {
}
