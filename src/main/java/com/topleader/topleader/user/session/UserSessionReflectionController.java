/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.topleader.topleader.exception.ApiValidationException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.topleader.topleader.exception.ErrorCodeConstants.NOT_EMPTY;
import static org.springframework.util.StringUtils.hasText;


/**
 * @author Daniel Slavik
 */
@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/user-sessions-reflection")
public class UserSessionReflectionController {

    private final UserSessionService userSessionService;

    @PostMapping
    public void setUserReflection(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody @Valid UserSessionReflectionRequest request
    ) {
        if (request.areaOfDevelopment() != null) {
            if (request.areaOfDevelopment().isEmpty()) {
                throw new ApiValidationException(NOT_EMPTY, "areaOfDevelopment", "[]", "cannot be empty");
            }
            if (!hasText(request.longTermGoal())) {
                throw new ApiValidationException(NOT_EMPTY, "longTermGoal", request.longTermGoal(), "cannot be empty");
            }
        }

        userSessionService.setUserSessionReflection(user.getUsername(), request);
    }

    public record UserSessionReflectionRequest(
        @Size(min = 1, max = 2000)
        String reflection,
        List<NewActionStepDto> newActionSteps,
        Set<Long> checked,
        List<String> areaOfDevelopment,
        String longTermGoal
    ) {
    }

    record NewActionStepDto(String label, LocalDate date) implements ActionStep {
    }

}
