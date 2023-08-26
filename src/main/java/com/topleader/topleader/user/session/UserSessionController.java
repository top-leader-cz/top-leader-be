package com.topleader.topleader.user.session;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/user-sessions")
public class UserSessionController {

    private final UserSessionService userSessionService;

    @GetMapping
    public UserSessionDto getUserSession(@AuthenticationPrincipal UserDetails user) {
        return userSessionService.getUserSession(user.getUsername());
    }


    @PostMapping
    public UserSessionDto createUserSession(
        @AuthenticationPrincipal UserDetails user,
        @RequestBody @Valid UserSessionRequest request
    ) {
        return userSessionService.setUserSession(user.getUsername(), request);
    }


    public record UserSessionDto(
        List<String> areaOfDevelopment,
        String longTermGoal,
        String motivation,
        List<ActionStepDto> actionSteps
    ) {
    }

    record ActionStepDto(long id, String label, LocalDate date, Boolean checked) {
    }


    public record UserSessionRequest(
        @NotEmpty
        List<String> areaOfDevelopment,
        @Size(min = 1, max = 1000)
        String longTermGoal,
        @Size(min = 1, max = 2000)
        String motivation,
        List<NewActionStepDto> actionSteps
    ) {
    }

    record NewActionStepDto(String label, LocalDate date) {
    }
}
