package com.topleader.topleader.hr.domain;

import com.topleader.topleader.user.User;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.Set;

public record UpdateUserRequest(
        @NotEmpty
        String firstName,

        @NotEmpty
        String lastName,

        String username,

        @NotNull
        User.Status status,

        @NotEmpty
        String timeZone,

        @NotEmpty
        Set<User.Authority> authorities,

        @Pattern(regexp = "[a-z]{2}")
        String locale,

        String manager,

        boolean isManager,

        String position

) implements UserRequest {
}
