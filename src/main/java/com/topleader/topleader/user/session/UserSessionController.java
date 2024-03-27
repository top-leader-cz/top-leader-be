package com.topleader.topleader.user.session;

import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoService;
import com.topleader.topleader.util.common.user.UserUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;


@RestController
@AllArgsConstructor
@RequestMapping("/api/latest/user-sessions")
public class UserSessionController {

    private final UserSessionService userSessionService;

    private final AiClient aiClient;

    private final UserInfoService userInfoService;

    private final UserDetailService userDetailService;

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

    @PostMapping("/generate-long-term-goal")
    public String generateLongTermGoal(@AuthenticationPrincipal UserDetails user, @RequestBody ActionSteps actionStepDto) {
        var userInfo = userInfoService.find(user.getUsername());
        var locale = userDetailService.getUser(user.getUsername()).orElse(new User().setLocale("en")).getLocale();
        return aiClient.findLongTermGoal(UserUtils.localeToLanguage(locale), userInfo.getStrengths(), userInfo.getValues(), actionStepDto.areaOfDevelopment());
    }

    @PostMapping("/generate-action-steps")
    public String generateActionStep(@AuthenticationPrincipal UserDetails user, @RequestBody ActionSteps actionStepDto) {
        var userInfo = userInfoService.find(user.getUsername());
        var locale = userDetailService.getUser(user.getUsername()).orElse(new User().setLocale("en")).getLocale();
        return aiClient.findActionsSteps(UserUtils.localeToLanguage(locale), userInfo.getStrengths(), userInfo.getValues(), actionStepDto.areaOfDevelopment(), actionStepDto.longTermGoal());
    }

    public  record ActionSteps(String areaOfDevelopment, String longTermGoal) {

    }

    public record UserSessionDto(
        List<String> areaOfDevelopment,
        String longTermGoal,
        String motivation,
        List<ActionStepDto> actionSteps,
        String lastReflection
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
