/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.common.password;

import com.topleader.topleader.common.exception.ApiValidationException;
import com.topleader.topleader.user.UserRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.common.exception.ErrorCodeConstants.INVALID_PASSWORD;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/password")
public class PasswordController {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;

    @PostMapping
    public void changeUserPassword(@AuthenticationPrincipal UserDetails user, @Valid @RequestBody ChangePasswordRequestDto request) {
        final var dbUser = userRepository.findById(user.getUsername()).orElseThrow();

        if (!passwordEncoder.matches(request.oldPassword(), dbUser.getPassword())) {
            throw new ApiValidationException(INVALID_PASSWORD, "oldPassword", null, "Invalid password");
        }

        userRepository.save(dbUser
            .setPassword(passwordEncoder.encode(request.newPassword())
            )
        );


    }

    public record ChangePasswordRequestDto(
        @NotEmpty String oldPassword,
        @NotEmpty String newPassword
    ) {
    }
}
