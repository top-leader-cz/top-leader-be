package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.common.ai.AiClient;
import com.topleader.topleader.user.UserDetailService;
import com.topleader.topleader.user.session.UserSessionService;
import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.userinfo.UserInfoService;
import com.topleader.topleader.user.userinsight.article.Article;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.common.util.common.CommonUtils;
import com.topleader.topleader.common.util.image.ArticleImageService;
import com.topleader.topleader.common.exception.NotFoundException;
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

        CommonUtils.tryRun(
            () -> userInsight.setUserArticles(jsonMapper.writeValueAsString(articles)),
            "Error converting user articles to JSON"
        );

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
                .orElseThrow(NotFoundException::new);
    }

    public record InsightItem(String text, boolean isPending) {
    }

    @Secured({"USER", "COACH", "ADMIN", "HR"})
    @PostMapping("/dashboard")
    public void dashboard(@AuthenticationPrincipal UserDetails authUser, @RequestBody DashboardRequest dashboardRequest) {
        var username = authUser.getUsername();
        log.info("Initiating dashboard content generation: [{}] ", username);
        var query = List.of(dashboardRequest.query());
        var userInsight = userInsightService.getInsight(username);
        userInsight.setActionGoalsPending(true);
        userInsight.setSuggestionPending(true);
        userInsightService.save(userInsight);
        var user = userDetailService.getUser(username).orElseThrow(NotFoundException::new);
        var userInfo = userInfoService.find(username);

        Thread.ofVirtual().start(() -> {
            try {
                var strengths = userInfo.getStrengths().stream().limit(5).toList();
                var suggestion = aiClient.generateSuggestion(username, dashboardRequest.query, strengths, userInfo.getValues(), user.getLocale());
                var insight = userInsightService.getInsight(username);
                insight.setSuggestion(suggestion);
                insight.setSuggestionPending(false);
                userInsightService.save(insight);
                log.info("Suggestion generated: [{}] ", username);
            } catch (Exception e) {
                var insight = userInsightService.getInsight(username);
                insight.setSuggestionPending(false);
                userInsightService.save(insight);
                log.error("Error generating suggestions [{}]", username, e);
            }
        });

        Thread.ofVirtual().start(() -> {
            try {
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
                log.info("Videos and articles generated: [{}] ", username);
            } catch (Exception e) {
                var insight = userInsightService.getInsight(username);
                insight.setActionGoalsPending(false);
                userInsightService.save(insight);
                log.error("Error converting user articles to JSON", e);
            }
        });

        log.info("dashboard content process initiated: [{}] ", username);

    }

    public record DashboardRequest(@NotEmpty String query) {

    }


}
