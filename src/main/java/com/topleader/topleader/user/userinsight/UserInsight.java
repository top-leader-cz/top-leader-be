package com.topleader.topleader.user.userinsight;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.experimental.Accessors;


@Data
@Entity
@Accessors(chain = true)
public class UserInsight {

    @Id
    private String username;

    private String leadershipStyleAnalysis;

    private String animalSpiritGuide;

    private String leadershipTip;

    private String personalGrowthTip;
}
