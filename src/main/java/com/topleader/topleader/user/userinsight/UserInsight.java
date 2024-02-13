package com.topleader.topleader.user.userinsight;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

import java.time.LocalDateTime;


@Data
@Entity
public class UserInsight {

    @Id
    private String username;

    private String leadershipStyleAnalysis;

    private String animalSpiritGuide;

    private String leadershipTip;

    private String personalGrowthTip;

    private LocalDateTime tipsGeneratedAt;
}
