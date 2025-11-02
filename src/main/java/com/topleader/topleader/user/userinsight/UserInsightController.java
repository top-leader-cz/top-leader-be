package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.session.UserSessionService;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.userinfo.UserInfoService;
import com.topleader.topleader.user.userinsight.article.Article;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.util.common.JsonUtils;
import com.topleader.topleader.util.image.ArticleImageService;
import io.vavr.control.Try;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RestController
@RequestMapping("/api/latest/user-insight")
@RequiredArgsConstructor
public class UserInsightController {

    public final UserInsightService userInsightService;
    public final ArticleRepository articlesRepository;
    public final ObjectMapper objectMapper;
    public final ArticleImageService articleImageService;
    public final UserSessionService userSessionService;
    private final UserInfoService userInfoService;
    private final UserDetailService userDetailService;
    public final AiClient aiClient;

    @GetMapping
    public Map<String, InsightItem> getInsight(@AuthenticationPrincipal UserDetails user) {
        final var userInsight = userInsightService.getInsight(user.getUsername());
        var articles = articlesRepository.findByUsername(user.getUsername()).stream()
                .map(article -> {
                    var content = article.getContent();
                    content.setId(article.getId());
                    content.setImageData(articleImageService.getImageAsBase64(content.getImageUrl()));
                    return content;
                })
                .toList();

        Try.run(() -> userInsight.setUserArticles(objectMapper.writeValueAsString(articles)))
                .onFailure(e -> log.error("Error converting user articles to JSON", e));

        return Map.of(
                "leaderShipStyle", new InsightItem(userInsight.getLeadershipStyleAnalysis(), userInsight.isLeadershipPending()),
                "leaderPersona", new InsightItem(userInsight.getWorldLeaderPersona(), userInsight.isLeadershipPending()),
                "animalSpirit", new InsightItem(userInsight.getAnimalSpiritGuide(), userInsight.isAnimalSpiritPending()),
                "leadershipTip", new InsightItem(userInsight.getLeadershipTip(), userInsight.isDailyTipsPending()),
                "personalGrowthTip", new InsightItem(userInsight.getPersonalGrowthTip(), userInsight.isActionGoalsPending()),
                "userPreviews", new InsightItem(userInsight.getUserPreviews(), userInsight.isActionGoalsPending()),
                "userArticles", new InsightItem(userInsight.getUserArticles(), userInsight.isActionGoalsPending())
        );
    }

    @GetMapping("/generate-tips")
    public void generateTips(@AuthenticationPrincipal UserDetails user) {
        userInsightService.generateTipsAsync(user.getUsername());
    }

    @Secured({"USER"})
    @GetMapping("/article/{articleId}")
    public UserArticle fetchArticle(@PathVariable long articleId) {
        return articlesRepository.findById(articleId)
                .map(article -> {
                    article.getContent().setId(article.getId());
                    article.getContent().setImageData(articleImageService.getImageAsBase64(article.getContent().getImageUrl()));
                    return article.getContent();
                })
                .orElseThrow(() -> new EntityNotFoundException("Article not found for id " + articleId));
    }

    public record InsightItem(String text, boolean isPending) {
    }

    @Secured({"USER"})
    @PostMapping("/dashboard")
    public void dashboard(@AuthenticationPrincipal UserDetails authUser, @RequestBody DashboardRequest dashboardRequest) {
        var username = authUser.getUsername();
        var query = List.of(dashboardRequest.query());
        var userInsight = userInsightService.getInsight(username);
        userInsight.setActionGoalsPending(true);
        var user = userDetailService.getUser(username).orElseThrow(EntityNotFoundException::new);
        var userInfo = userInfoService.find(username);

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {

            var previewsFuture = CompletableFuture.supplyAsync(
                    () -> userSessionService.handleUserPreview(username, query),
                    executor
            );

            var articlesFuture = CompletableFuture.supplyAsync(
                    () -> userSessionService.handleUserArticles(username, query),
                    executor
            );

            var suggestionFuture = CompletableFuture.supplyAsync(
                    () -> aiClient.generateSuggestion(username, dashboardRequest.query, userInfo.getStrengths(), userInfo.getValues(), user.getLocale()),
                    executor
            );


            var previews = previewsFuture.join();
            var articles = articlesFuture.join();
            var suggestion = suggestionFuture.join();

            userInsight.setUserPreviews(previews);

            if (!articles.isEmpty()) {
                articlesRepository.deleteAllByUsername(username);
                articles.forEach(article -> articlesRepository.save(new Article()
                        .setUsername(username)
                        .setContent(article))
                );
            }
            userInsight.setSuggestion(suggestion);

            userInsight.setActionGoalsPending(false);
            userInsightService.save(userInsight);
        }

    }

    public record DashboardRequest(@NotEmpty String query) {

    }


}
