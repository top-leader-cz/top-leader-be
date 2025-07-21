package com.topleader.topleader.user.userinsight;

import com.topleader.topleader.user.userinsight.article.ArticleRepository;
import com.topleader.topleader.util.common.JsonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/latest/user-insight")
@RequiredArgsConstructor
public class UserInsightController {

    public final UserInsightService userInsightService;
    public final ArticleRepository articlesRepository;
    public final ObjectMapper objectMapper;

    @GetMapping
    public Map<String, InsightItem> getInsight(@AuthenticationPrincipal UserDetails user) {
        var userInsight = userInsightService.getInsight(user.getUsername());
        var articles = articlesRepository.findByUsername(user.getUsername());
        try {
            var arrayNode = objectMapper.createArrayNode();
            articles.forEach(article -> {
                var contentNode = JsonUtils.toJObjectNode(article.getContent());
                    contentNode.put("id", article.getId());
                    arrayNode.add(contentNode);
            });
            userInsight.setUserArticles(arrayNode.toString());
        } catch (Exception e) {
            userInsight.setUserArticles("[]");
        }

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
