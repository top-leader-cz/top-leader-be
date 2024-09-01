/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoService;
import com.topleader.topleader.user.userinsight.UserInsightService;
import com.topleader.topleader.util.common.user.UserUtils;
import io.vavr.control.Try;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.engine.internal.Collections;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;


/**
 * @author Daniel Slavik
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserSessionService {

    private static final int INVALID_LINK = 404;

    private final UserInfoService userInfoService;

    private final UserDetailService userDetailService;

    private final UserActionStepRepository userActionStepRepository;

    private final DataHistoryRepository dataHistoryRepository;

    private final UserInsightService userInsightService;

    private final AiClient aiClient;

    private final ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    @Transactional
    public void setUserSessionReflection(String username, UserSessionReflectionController.UserSessionReflectionRequest request) {
        final var existingActionSteps = userActionStepRepository.findAllByUsername(username);

        deleteCheckActionSteps(existingActionSteps);

        final var userActionSteps = prepareActualActionSteps(
                username,
                existingActionSteps,
                Optional.ofNullable(request.checked()).orElse(Set.of()),
                Optional.ofNullable(request.newActionSteps()).orElse(List.of()));


        Executors.newVirtualThreadPerTaskExecutor().submit(() ->
                Try.run(() -> generateActionGoals(username, userActionSteps))
                        .onFailure(e -> log.error("Failed to generate action goals for user: [{}] ", username, e)));

        final var actualActionSteps = saveActionSteps(userActionSteps);

        Optional.ofNullable(request.areaOfDevelopment())
                .ifPresent(a -> userInfoService.setAreaOfDevelopmentAndLongTermGoal(username, a, request.longTermGoal()));

        createSessionStartHistoryData(
                username,
                userInfoService.setLastRestriction(username, request.reflection()),
                request.reflection(),
                actualActionSteps);
    }


    public UserSessionController.UserSessionDto getUserSession(String username) {
        return convertToUserSession(
                userInfoService.find(username),
                userActionStepRepository.findAllByUsername(username)
        );
    }

    @Transactional
    public UserSessionController.UserSessionDto setUserSession(String username, UserSessionController.UserSessionRequest request) {

        final var userInfo = safeSessionDataIntoTheUserInfoTable(username, request);

        deleteAllUserActionStep(username);

        final var userActionSteps = createNewUserActionSteps(username, request.actionSteps());

        createSessionStartHistoryData(username, userInfo, null, userActionSteps);

        final var steps = prepareActualActionSteps(
                username,
                List.of(),
                Set.of(),
                Optional.ofNullable(request.actionSteps()).orElse(List.of()));

        Executors.newVirtualThreadPerTaskExecutor().submit(() -> generateActionGoals(username, steps));

        saveActionSteps(userActionSteps);

        return convertToUserSession(userInfo, userActionSteps);
    }

    public void generateActionGoals(String username, List<UserActionStep> userActionSteps) {
        var user = userDetailService.find(username);
        var userInfo = userInfoService.find(username);
        var userInsight = userInsightService.getInsight(username);
        userInsight.setUsername(username);
        userInsight.setActionGoalsPending(true);
        userInsightService.save(userInsight);

        var actionGoals = userActionSteps.stream()
                .filter(step -> !step.getChecked())
                .map(UserActionStep::getLabel)
                .toList();

        userInsight.setPersonalGrowthTip(aiClient.findActionGoal(
                UserUtils.localeToLanguage(user.getLocale()),
                userInfo.getTopStrengths(),
                userInfo.getValues(),
                SessionUtils.getDevelopments(userInfo.getAreaOfDevelopment()),
                userInfo.getLongTermGoal(),
                actionGoals));

        if (!actionGoals.isEmpty()) {
            userInsight.setUserPreviews(handleUserPreview(username, actionGoals));
        }
        userInsight.setActionGoalsPending(false);

        userInsightService.save(userInsight);
    }

    public String handleUserPreview(String username, List<String> actionGoals) {
        var previews = Try.of(() -> {
                    var res = aiClient.generateUserPreviews(username, actionGoals);
                    var json = res.replace("```json", StringUtils.EMPTY).replace("```", StringUtils.EMPTY);
                    return objectMapper.readValue(json, new TypeReference<List<UserPreview>>() {
                    });
                })
                .onFailure(e -> log.error("Failed to generate user preview for user: [{}] ", username, e))
                .getOrElse(List.of());

        var filtered = previews.stream()
                .map(p -> {
                    var videoId = p.getUrl().split("v=")[1]; //
                    var thumbnail = String.format("http://i3.ytimg.com/vi/%s/hqdefault.jpg", videoId);
                    p.setThumbnail(thumbnail);
                    return p;
                })
                .filter(p -> urlValid(p.getThumbnail())
                ).toList();

        return Try.of(() -> objectMapper.writeValueAsString(filtered))
                .onFailure(e -> log.error("Failed to convert user preview for user: [{}] ", username, e))
                .getOrElse(() -> null);
    }

    private boolean urlValid(String url) {
        return Try.of(() -> restTemplate.getForEntity(url, String.class))
                .onFailure(e -> log.info("Failed to get url: [{}] ", url, e))
                .getOrElse(() -> ResponseEntity.status(404).body("Not Found"))
                .getStatusCode()
                .value() != INVALID_LINK;
    }

    public List<UserActionStep> prepareActualActionSteps(
            String username,
            List<UserActionStep> existingActionSteps,
            Set<Long> checked,
            List<? extends ActionStep> newActionStepDtos
    ) {
        return Stream.concat(
                existingActionSteps.stream()
                        .filter(not(UserActionStep::getChecked))
                        .map(step -> checked.contains(step.getId()) ? step.setChecked(true) : step),
                Optional.ofNullable(newActionStepDtos).orElse(List.of()).stream()
                        .map(step -> new UserActionStep()
                                .setChecked(false)
                                .setDate(step.date())
                                .setLabel(step.label())
                                .setUsername(username)
                        )
        ).toList();
    }

    private void deleteCheckActionSteps(List<UserActionStep> existingActionSteps) {
        Optional.of(existingActionSteps.stream().filter(UserActionStep::getChecked).toList())
                .filter(not(List::isEmpty))
                .ifPresent(userActionStepRepository::deleteAll);
    }


    public List<UserActionStep> saveActionSteps(List<UserActionStep> steps) {
        return Optional.ofNullable(steps)
                .filter(not(List::isEmpty))
                .map(userActionStepRepository::saveAll)
                .orElse(List.of());
    }

    private UserInfo safeSessionDataIntoTheUserInfoTable(String username, UserSessionController.UserSessionRequest request) {
        return userInfoService.setSessionData(
                username,
                request.areaOfDevelopment(),
                request.longTermGoal(),
                request.motivation()
        );
    }

    private void deleteAllUserActionStep(String username) {
        Optional.of(userActionStepRepository.findAllByUsername(username))
                .filter(not(List::isEmpty))
                .ifPresent(userActionStepRepository::deleteAll);

    }


    private List<UserActionStep> createNewUserActionSteps(String username, List<UserSessionController.NewActionStepDto> newActionStepDtos) {
        return Optional.ofNullable(newActionStepDtos)
                .filter(not(List::isEmpty))
                .map(steps -> userActionStepRepository.saveAll(
                        steps.stream()
                                .map(s -> new UserActionStep()
                                        .setUsername(username)
                                        .setChecked(false)
                                        .setLabel(s.label())
                                        .setDate(s.date())
                                )
                                .toList()
                ))
                .orElse(List.of());
    }


    private void createSessionStartHistoryData(String username, UserInfo userInfo, String reflection, List<UserActionStep> userActionSteps) {
        dataHistoryRepository.save(
                new DataHistory()
                        .setUsername(username)
                        .setCreatedAt(LocalDateTime.now())
                        .setType(DataHistory.Type.USER_SESSION)
                        .setData(new UserSessionStoredData()
                                .setAreaOfDevelopment(userInfo.getAreaOfDevelopment())
                                .setLongTermGoal(userInfo.getLongTermGoal())
                                .setMotivation(isNull(reflection) ? userInfo.getMotivation() : null)
                                .setReflection(reflection)
                                .setActionSteps(userActionSteps.stream().map(a -> new UserSessionStoredData.ActionStepData(
                                                        a.getId(), a.getLabel(), a.getDate(), a.getChecked()
                                                ))
                                                .toList()
                                )
                        )
        );
    }

    private UserSessionController.UserSessionDto convertToUserSession(UserInfo userInfo, List<UserActionStep> userActionSteps) {
        return new UserSessionController.UserSessionDto(
                userInfo.getAreaOfDevelopment(),
                userInfo.getLongTermGoal(),
                userInfo.getMotivation(),
                userActionSteps.stream()
                        .map(s -> new UserSessionController.ActionStepDto(s.getId(), s.getLabel(), s.getDate(), s.getChecked()))
                        .toList(),
                userInfo.getLastReflection()
        );
    }
}
