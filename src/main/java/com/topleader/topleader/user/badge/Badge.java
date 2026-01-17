package com.topleader.topleader.user.badge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Month;

@Table("badge")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    private String username;

    private String achievementType;

    private String month;

    private Integer year;

    public AchievementType getAchievementTypeEnum() {
        return achievementType != null ? AchievementType.valueOf(achievementType) : null;
    }

    public Badge setAchievementTypeEnum(AchievementType achievementType) {
        this.achievementType = achievementType != null ? achievementType.name() : null;
        return this;
    }

    public Month getMonthEnum() {
        return month != null ? Month.valueOf(month) : null;
    }

    public Badge setMonthEnum(Month month) {
        this.month = month != null ? month.name() : null;
        return this;
    }

    public enum AchievementType {
        COMPLETE_SESSION,
        WATCHED_VIDEO,
        COMPLETED_SHORT_GOAL
    }
}
