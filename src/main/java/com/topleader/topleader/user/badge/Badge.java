package com.topleader.topleader.user.badge;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Month;

@Table("badge")
@Data
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
public class Badge {

    @Id
    private Long id;

    private String username;

    private AchievementType achievementType;

    private Month month;

    private Integer year;

    public enum AchievementType {
        COMPLETE_SESSION,
        WATCHED_VIDEO,
        COMPLETED_SHORT_GOAL
    }
}
