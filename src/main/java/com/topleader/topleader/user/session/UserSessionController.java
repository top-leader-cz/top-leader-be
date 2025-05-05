package com.topleader.topleader.user.session;

import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.scheduled_session.ScheduledSessionRepository;
import com.topleader.topleader.user.User;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;
import com.topleader.topleader.user.session.feedback.SessionFeedback;
import com.topleader.topleader.user.session.feedback.SessionFeedbackRepository;
import com.topleader.topleader.user.userinfo.UserInfoService;
import com.topleader.topleader.util.common.user.UserUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.annotation.Secured;
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

    private final SessionFeedbackRepository sessionFeedbackRepository;

    private final ScheduledSessionRepository scheduledSessionRepository;

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
    public Collection<String> generateLongTermGoal(@AuthenticationPrincipal UserDetails user, @RequestBody ActionSteps actionStepDto) {
        var userInfo = userInfoService.find(user.getUsername());
        var locale = userDetailService.getUser(user.getUsername()).orElse(new User().setLocale("en")).getLocale();
        return split(aiClient.findLongTermGoal(UserUtils.localeToLanguage(locale), userInfo.getStrengths(), userInfo.getValues(), actionStepDto.areaOfDevelopment()));
    }

    @PostMapping("/generate-action-steps")
    public Collection<String> generateActionStep(@AuthenticationPrincipal UserDetails user, @RequestBody ActionSteps actionStepDto) {
        var userInfo = userInfoService.find(user.getUsername());
        var locale = userDetailService.getUser(user.getUsername()).orElse(new User().setLocale("en")).getLocale();
        return split(aiClient.findActionsSteps(UserUtils.localeToLanguage(locale), userInfo.getStrengths(), userInfo.getValues(), actionStepDto.areaOfDevelopment(), actionStepDto.longTermGoal()));
    }

    @GetMapping("/generate-recommended-growth")
    public List<RecommendedGrowth> generateRecommendedGrowth(@AuthenticationPrincipal UserDetails user) {
        return userSessionService.generateRecommendedGrowths(user.getUsername());
    }

    @Secured({"USER", "ADMIN"})
    @PostMapping("/feedback")
    public FeedbackDto setFeedback(@AuthenticationPrincipal UserDetails user, @RequestBody @Valid FeedbackDto request) {
        return scheduledSessionRepository.findTopByUsernameOrderByIdDesc(user.getUsername())
                .map(s -> sessionFeedbackRepository.save(new SessionFeedback()
                        .setId(new SessionFeedback.SessionFeedbackId(s.getId(), user.getUsername()))
                        .setAnswers(request.answers))
                        .setFeedback(request.feedback))
                .map(f -> new FeedbackDto(f.getId().getUsername(), f.getId().getSessionId(), f.getAnswers(), f.getFeedback()))
                .orElse(null);
    }


    public record FeedbackDto(String username, Long sessionId, @NotNull Map<String, Integer> answers, String feedback) {

    }



    Collection<String> split(String data) {
        return Arrays.stream(data
                .split("[0-9]\\."))
                .sequential()
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toSet());
    }


    public record ActionSteps(String areaOfDevelopment, String longTermGoal) {

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

    record NewActionStepDto(String label, LocalDate date) implements  ActionStep {
    }
}
