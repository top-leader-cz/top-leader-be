package com.topleader.topleader.user.userinsight;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


@Data
@Entity
@Accessors(chain = true)
@EqualsAndHashCode(of = {"username"})
public class UserInsight {

    @Id
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
