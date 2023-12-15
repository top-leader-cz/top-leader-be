/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoService;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import static java.util.Objects.isNull;
import static java.util.function.Predicate.not;


/**
 * @author Daniel Slavik
 */
@Service
@AllArgsConstructor
public class UserSessionService {

    private final UserInfoService userInfoService;

    private final UserActionStepRepository userActionStepRepository;

    private final DataHistoryRepository dataHistoryRepository;


    @Transactional
    public void setUserSessionReflection(String username, UserSessionReflectionController.UserSessionReflectionRequest request) {
        final var existingActionSteps = userActionStepRepository.findAllByUsername(username);

        deleteCheckActionSteps(existingActionSteps);

        final var userActionSteps = prepareActualActionSteps(
            username,
            existingActionSteps,
            Optional.ofNullable(request.checked()).orElse(Set.of()),
            Optional.ofNullable(request.newActionSteps()).orElse(List.of()));

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

        return convertToUserSession(userInfo, userActionSteps);
    }


    private List<UserActionStep> prepareActualActionSteps(
        String username,
        List<UserActionStep> existingActionSteps,
        Set<Long> checked,
        List<UserSessionReflectionController.NewActionStepDto> newActionStepDtos
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


    private List<UserActionStep> saveActionSteps(List<UserActionStep> steps) {
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
