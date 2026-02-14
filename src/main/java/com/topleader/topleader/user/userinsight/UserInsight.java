package com.topleader.topleader.user.userinsight;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.PersistenceCreator;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;


@Data
@Table("user_insight")
@Accessors(chain = true)
@EqualsAndHashCode(of = {"id"})
@NoArgsConstructor
@AllArgsConstructor(onConstructor_ = @PersistenceCreator)
public class UserInsight {

    @Id
    private Long id;

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
