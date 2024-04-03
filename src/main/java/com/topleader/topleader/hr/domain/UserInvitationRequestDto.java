package com.topleader.topleader.hr.domain;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;

public record UserInvitationRequestDto(
        @Email
        @NotEmpty
        String email,
        @NotEmpty
        String firstName,
        @NotEmpty
        String lastName,
        Boolean isTrial,
        @Pattern(regexp = "[a-z]{2}")
        String locale) {
}
