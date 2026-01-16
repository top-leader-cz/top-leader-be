/*
 * Copyright (c) 2023 Price f(x), s.r.o.
 */
package com.topleader.topleader.user.session;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.topleader.topleader.common.ai.AiClient;

import com.topleader.topleader.history.DataHistory;
import com.topleader.topleader.history.DataHistoryRepository;
import com.topleader.topleader.history.data.UserSessionStoredData;
import com.topleader.topleader.hr.company.Company;
import com.topleader.topleader.hr.company.CompanyRepository;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.badge.Badge;
import com.topleader.topleader.user.badge.BadgeService;
import com.topleader.topleader.user.session.domain.RecommendedGrowth;
import com.topleader.topleader.user.session.domain.UserPreview;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.userinfo.UserInfo;
import com.topleader.topleader.user.userinfo.UserInfoService;
import com.topleader.topleader.user.userinsight.UserInsightService;
import com.topleader.topleader.user.userinsight.article.Article;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.common.util.common.user.UserUtils;
import com.topleader.topleader.common.util.image.ArticleImageService;
import com.topleader.topleader.common.util.common.CommonUtils;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
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

    private final ObjectMapper jsonMapper;

    private final RestTemplate restTemplate;

    private final CompanyRepository companyRepository;

    private final BadgeService badgeService;

    private final ArticleImageService articleImageService;

    private final ArticleRepository articlesRepository;

    @Value("${thumbmail}")
    private String thumbmail;


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
                CommonUtils.tryRun(() -> generateActionGoals(username, userActionSteps),
                        "Failed to generate action goals for user: [" + username + "]"));

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
        badgeService.recordAchievement(username, Badge.AchievementType.COMPLETE_SESSION);

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
            var userArticles = handleUserArticles(username, actionGoals);
            if (!userArticles.isEmpty()) {
                articlesRepository.deleteAllByUsername(username);
                userArticles.forEach(article -> articlesRepository.save(new Article()
                        .setUsername(username)
                        .setContent(article))
                );
            }
        }
        userInsight.setActionGoalsPending(false);

        userInsightService.save(userInsight);
    }

    public String handleUserPreview(String username, List<String> actionGoals) {
          List<UserPreview> previews = CommonUtils.tryGetOrElse(() -> {
                    var res = aiClient.generateUserPreviews(username, actionGoals);
                    return jsonMapper.readValue(res, new TypeReference<List<UserPreview>>() {});
                },
                List.of(),
                "Failed to generate user preview for user: [" + username + "]");

        var filtered = previews.stream()
                .map(p -> {
                    try {
                        var videoId = p.getUrl().split("v=")[1];
                        var thumbnail = String.format(thumbmail, videoId);
                        p.setThumbnail(thumbnail);
                        return p;
                    } catch (Exception e) {
                        log.warn("Failed to parse video url for user: [{}] url: [{}] ", username, p.getUrl(), e);
                        return p;
                    }
                })
                .filter(p -> urlValid(p.getThumbnail())
                ).toList();

        return CommonUtils.tryGetOrNull(
                () -> jsonMapper.writeValueAsString(filtered),
                "Failed to convert user preview for user: [" + username + "]");
    }

    public List<UserArticle> handleUserArticles(String username, List<String> actionGoals) {
        var user = userDetailService.find(username);
        List<UserArticle> articles = CommonUtils.tryGetOrElse(
                () -> aiClient.generateUserArticles(username, actionGoals, UserUtils.localeToLanguage(user.getLocale())),
                List.of(),
                "Failed to generate user articles for user: [" + username + "]");
        return articles.stream()
                .map(article -> {
                    if (!urlValid(article.getUrl())) {
                        article.setUrl(null);
                    }
                    return article;
                })
                .map(article -> article.setImageUrl(articleImageService.generateImage(article.getImagePrompt())))
                .toList();
    }

    private boolean urlValid(String url) {
        return CommonUtils.tryGetOrElse(
                () -> restTemplate.getForEntity(url, String.class),
                ResponseEntity.status(404).body("Not Found"),
                "Failed to get url: [" + url + "]")
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

    @SneakyThrows
    public List<RecommendedGrowth> generateRecommendedGrowths(String username) {
        log.warn("generating recommend growth");
        var user = userDetailService.getUser(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        var company = companyRepository.findById(user.getCompanyId()).orElse(Company.empty());

        var businessStrategy = company.getBusinessStrategy();
        var position = user.getPosition();
        var aspiredCompetency = user.getAspiredCompetency();
        if (!canGenerateRecommendedGrowths(businessStrategy, position, aspiredCompetency)) {
            log.warn("Cannot generate recommended growths for user. Requirement dis not met");
            return null;
        }
        return CommonUtils.tryGetOrNull(
                () -> aiClient.generateRecommendedGrowths(user, businessStrategy, position, aspiredCompetency),
                "Failed to generate recommended growths");
    }

    boolean canGenerateRecommendedGrowths(String businessStrategy, String position, String competency) {
        return StringUtils.isNotBlank(businessStrategy) && StringUtils.isNotBlank(competency) && StringUtils.isNotBlank(position);
    }

}
