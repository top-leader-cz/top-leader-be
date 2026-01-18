package com.topleader.topleader.user.badge;


import com.topleader.topleader.IntegrationTest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
                .andExpect(jsonPath("$.completedSession", is(true)))
                .andExpect(jsonPath("$.completedShortTermGoal", is(true)))
                .andExpect(jsonPath("$.watchedVideo", is(true)))
                .andExpect(jsonPath("$.badges").isNotEmpty());

    }

    private Badge badge(Badge.AchievementType type) {
        var now = LocalDate.now();
        return new Badge().setUsername("jakub.svezi@dummy.com").setAchievementType(type).setMonth(now.getMonth()).setYear(now.getYear());
    }

    @Test
    @WithUserDetails("jakub.svezi@dummy.com")
    void watchedVideo() throws Exception {
        mvc.perform(post("/api/latest/user-badges/watched-video"))
                .andExpect(status().isOk());

        Assertions.assertThat(badgeRepository.findOne(Example.of(badge(Badge.AchievementType.WATCHED_VIDEO)))).isNotEmpty();
    }

 }