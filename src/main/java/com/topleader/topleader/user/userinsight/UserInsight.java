package com.topleader.topleader.user.userinsight;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import com.topleader.topleader.common.entity.BaseEntity;


@Data
@Table("user_insight")
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
public class UserInsight extends BaseEntity {
    private String username;

    private String leadershipStyleAnalysis;

    private String worldLeaderPersona;

    private boolean leadershipPending;

    private String animalSpiritGuide;

    private boolean animalSpiritPending;

    private String leadershipTip;

    private String personalGrowthTip;

    private boolean dailyTipsPending;

    private LocalDateTime tipsGeneratedAt;

    private String userPreviews;

    private String userArticles;

    private String suggestion;

    private boolean suggestionPending;

    private boolean actionGoalsPending;
}
