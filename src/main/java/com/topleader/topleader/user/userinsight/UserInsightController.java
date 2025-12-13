package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.ai.AiClient;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.session.UserSessionService;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.userinfo.UserInfoService;
import com.topleader.topleader.user.userinsight.article.Article;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
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

import tools.jackson.databind.json.JsonMapper;

@Slf4j
@RestController
@RequestMapping("/api/latest/user-insight")
@RequiredArgsConstructor
public class UserInsightController {

    public final UserInsightService userInsightService;
    public final ArticleRepository articlesRepository;
    public final JsonMapper jsonMapper;
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

        Try.run(() -> userInsight.setUserArticles(jsonMapper.writeValueAsString(articles)))
                .onFailure(e -> log.error("Error converting user articles to JSON", e));

        return Map.of(
                "leaderShipStyle", new InsightItem(userInsight.getLeadershipStyleAnalysis(), userInsight.isLeadershipPending()),
                "leaderPersona", new InsightItem(userInsight.getWorldLeaderPersona(), userInsight.isLeadershipPending()),
                "animalSpirit", new InsightItem(userInsight.getAnimalSpiritGuide(), userInsight.isAnimalSpiritPending()),
                "leadershipTip", new InsightItem(userInsight.getLeadershipTip(), userInsight.isDailyTipsPending()),
                "personalGrowthTip", new InsightItem(userInsight.getPersonalGrowthTip(), userInsight.isActionGoalsPending()),
                "userPreviews", new InsightItem(userInsight.getUserPreviews(), userInsight.isActionGoalsPending()),
                "userArticles", new InsightItem(userInsight.getUserArticles(), userInsight.isActionGoalsPending()),
                "suggestion", new InsightItem(userInsight.getSuggestion(), userInsight.isSuggestionPending()));
    }

    @GetMapping("/generate-tips")
    public void generateTips(@AuthenticationPrincipal UserDetails user) {
        userInsightService.generateTipsAsync(user.getUsername());
    }

    @Secured({"USER", "COACH", "ADMIN", "HR"})
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

    @Secured({"USER", "COACH", "ADMIN", "HR"})
    @PostMapping("/dashboard")
    public void dashboard(@AuthenticationPrincipal UserDetails authUser, @RequestBody DashboardRequest dashboardRequest) {
        var username = authUser.getUsername();
        var query = List.of(dashboardRequest.query());
        var userInsight = userInsightService.getInsight(username);
        userInsight.setActionGoalsPending(true);
        userInsight.setSuggestionPending(true);
        userInsightService.save(userInsight);
        var user = userDetailService.getUser(username).orElseThrow(EntityNotFoundException::new);
        var userInfo = userInfoService.find(username);

        Thread.ofVirtual().start(() ->
                Try.run(() -> {
                            var suggestion = aiClient.generateSuggestion(username, dashboardRequest.query, userInfo.getStrengths(), userInfo.getValues(), user.getLocale());
                            var insight = userInsightService.getInsight(username);
                            insight.setSuggestion(suggestion);
                            insight.setSuggestionPending(false);
                            userInsightService.save(insight);
                        }).onSuccess(result -> log.info("Suggestion generated: [{}] ", username))
                        .onFailure(e -> {
                                    var insight = userInsightService.getInsight(username);
                                    insight.setSuggestionPending(false);
                                    userInsightService.save(insight);
                                    log.error("Error generating suggestions [{}]", username, e);
                                }
                        ));

        Thread.ofVirtual().start(() ->
                Try.run(() -> {
                            var previews = userSessionService.handleUserPreview(username, query);
                            var articles = userSessionService.handleUserArticles(username, query);
                            var insight = userInsightService.getInsight(username);
                            insight.setUserPreviews(previews);
                            userInsightService.save(insight);
                            if (!articles.isEmpty()) {
                                articlesRepository.deleteAllByUsername(username);
                                articles.forEach(article -> articlesRepository.save(new Article()
                                        .setUsername(username)
                                        .setContent(article))
                                );
                            }
                            insight.setActionGoalsPending(false);
                            userInsightService.save(insight);
                        }).onFailure(e -> {
                            var insight = userInsightService.getInsight(username);
                            insight.setActionGoalsPending(false);
                            userInsightService.save(insight);
                            log.error("Error converting user articles to JSON", e);
                        })
                        .onSuccess(i -> log.info("Videos and articles generated: [{}] ", username))
        );


    }

    public record DashboardRequest(@NotEmpty String query) {

    }


}
