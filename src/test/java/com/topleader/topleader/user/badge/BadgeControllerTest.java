package com.topleader.topleader.user.badge;


import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql("/sql/user/badges/user-badges.sql")
public class BadgeControllerTest extends IntegrationTest {

    @Autowired
    private BadgeRepository badgeRepository;

    @Test
    @WithUserDetails("jakub.svezi@dummy.com")
    void getBadges() throws Exception {
        badgeRepository.save(badge(Badge.AchievementType.WATCHED_VIDEO));
        badgeRepository.save(badge(Badge.AchievementType.COMPLETE_SESSION));
        badgeRepository.save(badge(Badge.AchievementType.COMPLETED_SHORT_GOAL));

        mvc.perform(get("/api/latest/user-badges"))
                        .andExpect(status().isOk())
                .andExpect(content().json("""
                                         
                               {
                          "completedSession": true,
                          "completedShortTermGoal": true,
                          "watchedVideo": true,
                          "badges": {
                            "JANUARY": false,
                            "FEBRUARY": false,
                            "MARCH": false,
                            "APRIL": false,
                            "MAY": true
                          }
                        }

                               """));
    }

    private Badge badge(Badge.AchievementType type) {
        var now = LocalDate.parse("2025-05-20");
        return new Badge().setBadgeId(new Badge.BadgeId("jakub.svezi@dummy.com", type, now.getMonth(), now.getYear()));
    }

    @Test
    @WithUserDetails("jakub.svezi@dummy.com")
    void watchedVideo() throws Exception {
        mvc.perform(post("/api/latest/user-badges/watched-video"))
                .andExpect(status().isOk());

        Assertions.assertThat(badgeRepository.findOne(Example.of(badge(Badge.AchievementType.WATCHED_VIDEO)))).isEmpty();
    }
}