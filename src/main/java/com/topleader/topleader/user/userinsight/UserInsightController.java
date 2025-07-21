package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.user.session.domain.UserArticle;
import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.util.common.JsonUtils;
import io.vavr.control.Try;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@RestController
@RequestMapping("/api/latest/user-insight")
@RequiredArgsConstructor
public class UserInsightController {

    public final UserInsightService userInsightService;
    public final ArticleRepository articlesRepository;
    public final ObjectMapper objectMapper;

    @GetMapping
    public Map<String, InsightItem> getInsight(@AuthenticationPrincipal UserDetails user) {
        final var userInsight = userInsightService.getInsight(user.getUsername());
        var articles = articlesRepository.findByUsername(user.getUsername()).stream()
                .map(article -> {
                    var content = article.getContent();
                    content.setId(article.getId());
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


    public record InsightItem(String text, boolean isPending) {
    }
}
